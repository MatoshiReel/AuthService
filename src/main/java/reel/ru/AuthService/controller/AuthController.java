package reel.ru.AuthService.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.web.bind.annotation.*;
import reel.ru.AuthService.model.jpa.entity.Account;
import reel.ru.AuthService.model.jpa.repository.AccountRepository;
import reel.ru.AuthService.model.security.encryption.EncoderFactory;
import reel.ru.AuthService.model.error.ErrorMessageFactory;
import reel.ru.AuthService.model.error.FieldError;
import reel.ru.AuthService.model.error.Reason;
import reel.ru.AuthService.model.parser.JsonParser;
import reel.ru.AuthService.model.security.token.JwtCreator;
import reel.ru.AuthService.model.security.token.RSAJwtCreator;
import reel.ru.AuthService.model.validation.AccountValidator;

import java.security.*;
import java.util.Base64;

@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Value("${ENC_AES_SECRET_KEY}")
    private String secretKeyAES;
    @Value("${ENC_AES_SALT}")
    private String saltAES;
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final JwtCreator jwtCreator;

    public AuthController(AccountRepository accountRepository, AccountValidator accountValidator, JwtCreator jwtCreator) {
        this.accountRepository = accountRepository;
        this.accountValidator = accountValidator;
        this.jwtCreator = jwtCreator;
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
                return ResponseEntity.badRequest().body(FieldError.builder().reason(Reason.DECRYPTION).message(ErrorMessageFactory.get(Reason.DECRYPTION)).build());
            } catch(JsonProcessingException e) {
                logger.error("JSON parameters don't match the class object being deserialized : {}", jsonAccountData, e);
                return ResponseEntity.badRequest().build();
            }
        }
        FieldError fieldError = accountValidator.validate(account, AccountValidator.Mode.SIGN_UP);
        if(fieldError != null) return ResponseEntity.badRequest().body(fieldError);
        Account newAccount = Account.builder().login(account.getLogin()).password(EncoderFactory.getArgon2Encoder().encode(account.getPassword())).build();
        accountRepository.save(newAccount);
        return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Bearer %s", jwtCreator.create(newAccount.getId().toString(), 30L * 86_400_000)));
    }

    @PostMapping("/signin")
    private ResponseEntity<Object> signIn(@RequestBody(required = false) String encryptedAccountData, JsonParser<Account> jsonParser) {
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
                return ResponseEntity.badRequest().body(FieldError.builder().reason(Reason.DECRYPTION).message(ErrorMessageFactory.get(Reason.DECRYPTION)).build());
            } catch(JsonProcessingException e) {
                logger.error("JSON parameters don't match the class object being deserialized : {}", jsonAccountData, e);
                return ResponseEntity.badRequest().build();
            }
        }
        FieldError fieldError = accountValidator.validate(account, AccountValidator.Mode.SIGN_IN);
        if(fieldError != null) return ResponseEntity.badRequest().body(fieldError);
        Account savedAccount = accountRepository.findByLogin(account.getLogin());
        if(!savedAccount.getIs2FaEnabled()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(String.format("Bearer %s", jwtCreator.create(savedAccount.getId().toString(), 30L * 86_400_000)));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/otp/verify")
    private ResponseEntity<?> otpVerify(@RequestBody String code) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
