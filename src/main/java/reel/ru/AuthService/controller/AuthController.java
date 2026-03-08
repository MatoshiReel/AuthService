package reel.ru.AuthService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reel.ru.AuthService.jpa.entity.Account;
import reel.ru.AuthService.model.error.FieldError;
import reel.ru.AuthService.model.validation.AccountValidator;
import reel.ru.AuthService.model.validation.SwitchableValidator;

@RestController
@RequestMapping(path="/auth")
public class AuthController {
    private final SwitchableValidator<Account, AccountValidator.Mode> accountValidator;

    public AuthController(SwitchableValidator<Account, AccountValidator.Mode> accountValidator) {
        this.accountValidator = accountValidator;
    }

    @PostMapping("/signup")
    private ResponseEntity<Object> signUp(@RequestBody(required = false) Account bodyAccount) {
        if(bodyAccount == null) {
            return ResponseEntity.badRequest().build();
        }
        FieldError fieldError = accountValidator.validate(bodyAccount, AccountValidator.Mode.SIGN_UP);
        if(fieldError != null) return ResponseEntity.badRequest().body(fieldError);
        return ResponseEntity.status(HttpStatus.CREATED).body("Bearer <Token>");
    }

    @PostMapping("/signin")
    private ResponseEntity<?> signIn() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/otp/verify")
    private ResponseEntity<?> otpVerify(@RequestBody String code) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
