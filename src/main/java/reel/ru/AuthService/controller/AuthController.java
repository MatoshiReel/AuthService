package reel.ru.AuthService.controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.web.bind.annotation.*;
import reel.ru.AuthService.converter.AccountConverter;
import reel.ru.AuthService.dto.AccountDto;
import reel.ru.AuthService.port.messaging.producer.UserRegisteredEventPublisher;
import reel.ru.AuthService.repository.RoleRepository;
import reel.ru.AuthService.service.error.*;
import reel.ru.AuthService.entity.Account;
import reel.ru.AuthService.repository.AccountRepository;
import reel.ru.AuthService.service.security.otp.OtpGenerator;
import reel.ru.AuthService.service.security.otp.OtpKeyFormatter;
import reel.ru.AuthService.service.security.otp.OtpMailSender;
import reel.ru.AuthService.service.security.token.AccessTokenCreator;
import reel.ru.AuthService.service.validation.AccountValidator;
import reel.ru.AuthService.service.validation.ParamValidator;

import java.time.Duration;
import java.util.Objects;

@OpenAPIDefinition(info = @Info(title = "AuthService API", description = "API for user authentication and OTP verification", version = "1.0"))
@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final RedisOperations<String, String> redisOperations;
    private final Gson gson;
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    @Value("${ENC_AES_SECRET_KEY}")
    private String secretKeyAES;
    @Value("${ENC_AES_SALT}")
    private String saltAES;
    private final RoleRepository roleRepository;
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final AccessTokenCreator tokenCreator;
    private final long jwtExpiredTimeMillis = 30L * 86_400_000;
    private final OtpMailSender otpMailSender;
    private final Duration otpExpiredTimeMinutes = Duration.ofMinutes(10);
    private final UserRegisteredEventPublisher eventPublisher;

    public AuthController(RedisOperations<String, String> redisOperations, Gson gson, Argon2PasswordEncoder argon2PasswordEncoder, RoleRepository roleRepository, AccountRepository accountRepository, AccountValidator accountValidator, AccessTokenCreator tokenCreator, OtpMailSender otpMailSender, UserRegisteredEventPublisher eventPublisher) {
        this.redisOperations = redisOperations;
        this.gson = gson;
        this.argon2PasswordEncoder = argon2PasswordEncoder;
        this.roleRepository = roleRepository;
        this.accountRepository = accountRepository;
        this.accountValidator = accountValidator;
        this.tokenCreator = tokenCreator;
        this.otpMailSender = otpMailSender;
        this.eventPublisher = eventPublisher;
    }

    @Tag(name = "Sign Up")
    @Operation(summary = "Register a new user", description = "Creates a new user account using login, password and repeatedPassword")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Success sign up", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, examples = {@ExampleObject(value = "Bearer Access Token")})),
            @ApiResponse(responseCode = "400", description = "Invalid request body data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(oneOf = {FieldRequestError.class, RequestError.class}))),
            @ApiResponse(responseCode = "500", content = @Content)
    })
    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> signUp(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Account data, which consists login, password and repeatedPassword fields")
                                              @RequestBody(required = false) AccountDto accountDto, @Parameter(hidden = true) AccountConverter accountConverter) {
        if(accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RequestError.builder().reason(Reason.EMPTY).message(ErrorMessageFactory.get(Reason.EMPTY)).build());
        }
        /*AccountDto accountDto;
        if(encryptedAccountData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            String jsonAccountData = null;
            try {
                jsonAccountData = Encryptors.delux(this.secretKeyAES, this.saltAES).decrypt(encryptedAccountData);
                accountDto = gson.fromJson(jsonAccountData, AccountDto.class);
            } catch(IllegalArgumentException | IllegalStateException e) {
                logger.warn("url: auth/signup, Illegal encrypting data : {}", encryptedAccountData, e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(RequestError.builder().reason(Reason.DECRYPTION).message(ErrorMessageFactory.get(Reason.DECRYPTION)).build());
            } catch(JsonSyntaxException e) {
                logger.warn("url: auth/signup, JSON parameters don't match the class object being deserialized : {}", jsonAccountData, e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(RequestError.builder().reason(Reason.JSON_FORMAT).message(ErrorMessageFactory.get(Reason.JSON_FORMAT)).build());
            }
        }*/
        FieldRequestError error = accountValidator.validate(accountDto, AccountValidator.Mode.SIGN_UP);
        if(error != null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        Account account = accountConverter.convert(accountDto);
        account.setPassword(argon2PasswordEncoder.encode(account.getPassword()));
        account.setRole(roleRepository.findByName("USER"));
        accountRepository.save(account);
        eventPublisher.publish(account.getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.TEXT_PLAIN).body(String.format("Bearer %s", tokenCreator.create(account.getId().toString(), account.getRole().getName(), jwtExpiredTimeMillis)));
    }

    @Tag(name = "Sign In")
    @Operation(summary = "LogIn an user", description = "Give access for an user, if he doesn't have 2FA varification enabled or send OTP-code on user's email after success validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success sign in", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, examples = {@ExampleObject(value = "Bearer Access Token")})),
            @ApiResponse(responseCode = "202", description = "If account has 2FA by email", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, examples = @ExampleObject("email"))),
            @ApiResponse(responseCode = "400", description = "Invalid request body data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(oneOf = {FieldRequestError.class, RequestError.class}))),
            @ApiResponse(responseCode = "500", content = @Content)
    })
    @PostMapping(value = "/signin", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> signIn(@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Account data, which consists login and password fields")
                                              @RequestBody(required = false) AccountDto accountDto) {
        if(accountDto == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(RequestError.builder().reason(Reason.EMPTY).message(ErrorMessageFactory.get(Reason.EMPTY)).build());
        }
        /*AccountDto accountDto;
        if(encryptedAccountData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            String jsonAccountData = null;
            try {
                jsonAccountData = Encryptors.delux(this.secretKeyAES, this.saltAES).decrypt(encryptedAccountData);
                accountDto = gson.fromJson(jsonAccountData, AccountDto.class);
            } catch(IllegalArgumentException | IllegalStateException e) {
                logger.warn("url: auth/signin, Illegal encrypting data : {}", encryptedAccountData, e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(RequestError.builder().reason(Reason.DECRYPTION).message(ErrorMessageFactory.get(Reason.DECRYPTION)).build());
            } catch(JsonSyntaxException e) {
                logger.warn("url: auth/signin, JSON parameters don't match the class object being deserialized : {}", jsonAccountData, e);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON).body(RequestError.builder().reason(Reason.JSON_FORMAT).message(ErrorMessageFactory.get(Reason.JSON_FORMAT)).build());
            }
        }*/
        FieldRequestError error = accountValidator.validate(accountDto, AccountValidator.Mode.SIGN_IN);
        if(error != null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        Account savedAccount = accountRepository.findByLogin(accountDto.login);
        if(!savedAccount.getIs2FaEnabled()) {
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(String.format("Bearer %s", tokenCreator.create(savedAccount.getId().toString(), savedAccount.getRole().getName(), jwtExpiredTimeMillis)));
        } else {
            String otp = OtpGenerator.generate(6);
            String email = savedAccount.getEmail();
            if(email != null) {
                otpMailSender.send(email, otp);
                redisOperations.opsForValue().set(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN), Objects.requireNonNull(argon2PasswordEncoder.encode(otp)), otpExpiredTimeMinutes);
                redisOperations.opsForValue().set(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN), String.valueOf(0), otpExpiredTimeMinutes);
                return ResponseEntity.status(HttpStatus.ACCEPTED).contentType(MediaType.TEXT_PLAIN).body(email);
            } else {
                logger.error("Email field of account for user with {} id is empty, but is2FaEnabled field is {}.", savedAccount.getId(), savedAccount.getIs2FaEnabled());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }
    }

    @Tag(name = "OTP", description = "2FA")
    @Operation(summary = "Verify an user by email", description = "Give access for an user by OTP-code, which was sent on user's email, when he made a sign in, if he has 2FA varification enabled")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success OTP verification", content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, examples = {@ExampleObject(value = "Bearer Access Token")})),
            @ApiResponse(responseCode = "400", description = "Invalid query params", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ParamRequestError.class))),
            @ApiResponse(responseCode = "403", description = "All Attempts were used and OTP doesn't exist more", content = @Content),
            @ApiResponse(responseCode = "500", content = @Content)
    })
    @PostMapping(value = "/otp/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    private ResponseEntity<Object> otpVerify(@RequestParam(value = "email", required = false) String email, @RequestParam(value = "otp", required = false) String otp, @Parameter(hidden = true) ParamValidator<String> paramValidator) {
        ParamRequestError error = paramValidator.validate(email, "email");
        if(error == null) error = paramValidator.validate(otp, "otp");
        if(error != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        String attempts = redisOperations.opsForValue().get(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
        if(attempts == null || Integer.parseInt(attempts) >= 5) {
            redisOperations.delete(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            redisOperations.delete(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String savedOtp = redisOperations.opsForValue().get(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
        if(!argon2PasswordEncoder.matches(otp, savedOtp)) {
            redisOperations.opsForValue().increment(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ParamRequestError.builder().param("otp").reason(Reason.NOT_MATCH).message(String.format(ErrorMessageFactory.get(Reason.NOT_MATCH), "otp")).build());
        } else {
            redisOperations.delete(OtpKeyFormatter.format(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            redisOperations.delete(OtpKeyFormatter.formatForAttempts(email, OtpKeyFormatter.OtpType.EMAIL, OtpKeyFormatter.OtpPurposeType.SIGN_IN));
            Account account = accountRepository.findByEmail(email);
            return ResponseEntity.status(HttpStatus.OK).contentType(MediaType.TEXT_PLAIN).body(String.format("Bearer %s", tokenCreator.create(account.getId().toString(), account.getRole().getName(), jwtExpiredTimeMillis)));
        }
    }
}
