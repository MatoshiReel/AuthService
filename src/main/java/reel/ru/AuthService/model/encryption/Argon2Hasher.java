package reel.ru.AuthService.model.encryption;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Argon2Hasher implements Hasher {
    private final Argon2PasswordEncoder encoder;

    public Argon2Hasher() {
        this.encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Override
    public String hash(String text) {
        return encoder.encode(text);
    }

    @Override
    public boolean matches(String text, String hashedText) {
        return encoder.matches(text, hashedText);
    }
}
