package reel.ru.AuthService.model.security.token;

public interface TokenCreator {
    String create(String id, long expiredTimeMillis);
}
