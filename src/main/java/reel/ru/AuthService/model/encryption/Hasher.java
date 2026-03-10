package reel.ru.AuthService.model.encryption;

public interface Hasher {
    String hash(String text);
    boolean matches(String text, String hashedText);
}
