package reel.ru.AuthService.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/auth")
public class AuthController {

    @PostMapping("/signup")
    private ResponseEntity<?> signUp() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
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
