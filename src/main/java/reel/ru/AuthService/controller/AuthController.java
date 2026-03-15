package reel.ru.AuthService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.web.bind.annotation.*;
import reel.ru.AuthService.model.error.*;
import reel.ru.AuthService.model.jpa.entity.Account;
import reel.ru.AuthService.model.jpa.repository.AccountRepository;
import reel.ru.AuthService.model.redis.RedisService;
import reel.ru.AuthService.model.security.encryption.EncoderFactory;
import reel.ru.AuthService.model.parser.JsonParser;
import reel.ru.AuthService.model.security.otp.OtpGenerator;
import reel.ru.AuthService.model.security.otp.OtpKeyFormatter;
import reel.ru.AuthService.model.security.otp.OtpMailSender;
import reel.ru.AuthService.model.security.token.TokenCreator;
import reel.ru.AuthService.model.validation.AccountValidator;
import reel.ru.AuthService.model.validation.ParamValidator;

import java.security.*;
import java.time.Duration;

@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final RedisService redisService;
    @Value("${ENC_AES_SECRET_KEY}")
    private String secretKeyAES;
    @Value("${ENC_AES_SALT}")
    private String saltAES;
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final TokenCreator tokenCreator;
    private final long jwtExpiredTimeMillis = 30L * 86_400_000;
    private final OtpMailSender otpMailSender;
    private final Duration otpExpiredTimeMinutes = Duration.ofMinutes(10);

    public AuthController(AccountRepository accountRepository, AccountValidator accountValidator, TokenCreator tokenCreator, RedisService redisService, OtpMailSender otpMailSender) {
        this.accountRepository = accountRepository;
        this.accountValidator = accountValidator;
        this.tokenCreator = tokenCreator;
        this.redisService = redisService;
        this.otpMailSender = otpMailSender;
    }

    @PostMapping("/signup")
    private ResponseEntity<Object> signUp(@RequestBody(required = false) String encryptedAccountData, JsonParser<Account> jsonParser) {
        Account account;
        if(encryptedAccountData == null) {
            return ResponseEntity.badRequest().build();
        } else {
            String jsonAccountData = null;
            try {
                jsonAccountData = Encryptors.delux(this.secretKeyAES, this.saltAES).decrypt(encryptedAccountData);
                account = jsonParser.parseToObject(jsonAccountData, Account.class);
            } catch(IllegalArgumentException e) {
                logger.error("Illegal encrypting data : {}", encryptedAccountData, e);
                return ResponseEntity.badRequest().body(RequestError.builder().reason(Reason.DECRYPTION).message(ErrorMessageFactory.get(Reason.DECRYPTION)).build());
            } catch(JsonProcessingException e) {
                logger.error("JSON parameters don't match the class object being deserialized : {}", jsonAccountData, e);
                return ResponseEntity.badRequest().build();
            }
        }
        FieldRequestError error = accountValidator.validate(account, AccountValidator.Mode.SIGN_UP);
        if(error != null) return ResponseEntity.badRequest().body(error);
        Account newAccount = Account.builder().login(account.getLogin()).password(EncoderFactory.getArgon2Encoder().encode(account.getPassword())).build();
        accountRepository.save(newAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Bearer %s", tokenCreator.create(newAccount.getId().toString(), jwtExpiredTimeMillis)));
    }

    @PostMapping("/signin")
    private ResponseEntity<Object> signIn(@RequestBody(required = false) String encryptedAccountData, JsonParser<Account> jsonParser, RedisService redisService) {
        Account account;
        if(encryptedAccountData == null) {
            return ResponseEntity.badRequest().build();
        } else {
            String jsonAccountData = null;
            try {
                jsonAccountData = Encryptors.delux(this.secretKeyAES, this.saltAES).decrypt(encryptedAccountData);
                account = jsonParser.parseToObject(jsonAccountData, Account.class);
            } catch(IllegalArgumentException e) {
                logger.error("Illegal encrypting data : {}", encryptedAccountData, e);
                return ResponseEntity.badRequest().body(RequestError.builder().reason(Reason.DECRYPTION).message(ErrorMessageFactory.get(Reason.DECRYPTION)).build());
            } catch(JsonProcessingException e) {
                logger.error("JSON parameters don't match the class object being deserialized : {}", jsonAccountData, e);
                return ResponseEntity.badRequest().build();
            }
        }
        FieldRequestError error = accountValidator.validate(account, AccountValidator.Mode.SIGN_IN);
        if(error != null) return ResponseEntity.badRequest().body(error);
        Account savedAccount = accountRepository.findByLogin(account.getLogin());
        if(!savedAccount.getIs2FaEnabled()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Bearer %s", tokenCreator.create(savedAccount.getId().toString(), jwtExpiredTimeMillis)));
        } else {
            String otp = OtpGenerator.generate(6);
            String email = savedAccount.getEmail();
            if(email != null) {
                otpMailSender.send(email, otp);
                redisService.getRedisOperations().opsForValue().set(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN), EncoderFactory.getArgon2Encoder().encode(otp), otpExpiredTimeMinutes);
                redisService.getRedisOperations().opsForValue().set(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN), String.valueOf(0), otpExpiredTimeMinutes);
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(email);
            } else {
                logger.error("Email field of account for user with {} id is empty, but is2FaEnabled field is {}.", savedAccount.getId(), savedAccount.getIs2FaEnabled());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @PostMapping("/otp/verify")
    private ResponseEntity<Object> otpVerify(@RequestParam("email") String email, @RequestParam("otp") String otp, ParamValidator<String> paramValidator) {
        ParamRequestError error = paramValidator.validate(email, "email");
        if(error == null) error = paramValidator.validate(otp, "otp");
        if(error != null) return ResponseEntity.badRequest().body(error);
        String attempts = redisService.getRedisOperations().opsForValue().get(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
        if(attempts == null || Integer.parseInt(attempts) >= 5) {
            redisService.getRedisOperations().delete(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            redisService.getRedisOperations().delete(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String savedOtp = redisService.getRedisOperations().opsForValue().get(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
        if(savedOtp == null || !EncoderFactory.getArgon2Encoder().matches(otp, savedOtp)) {
            redisService.getRedisOperations().opsForValue().increment(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ParamRequestError.builder().param("otp").reason(Reason.NOT_MATCH).message(String.format(ErrorMessageFactory.get(Reason.NOT_MATCH), "otp")).build());
        } else {
            redisService.getRedisOperations().delete(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            redisService.getRedisOperations().delete(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            Account account = accountRepository.findByEmail(email);
            return ResponseEntity.ok(String.format("Bearer %s", tokenCreator.create(account.getId().toString(), jwtExpiredTimeMillis)));
        }
    }

    //TODO
    @ResponseBody
    @GetMapping("/encrypt")
    private String encrypt(@RequestBody String data) {
        return Encryptors.delux(this.secretKeyAES, this.saltAES).encrypt(data);
    }

    //TODO
    @ResponseBody
    @GetMapping("/decrypt")
    private String decrypt(@RequestBody String encryptedData) {
        return Encryptors.delux(this.secretKeyAES, this.saltAES).decrypt(encryptedData);
    }
}
