package reel.ru.AuthService.service.security.encryption;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class EncoderFactory {
    public static Argon2PasswordEncoder getArgon2Encoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}
