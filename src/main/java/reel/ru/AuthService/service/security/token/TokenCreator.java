package reel.ru.AuthService.service.security.token;

public interface TokenCreator {
    String create(String id, long expiredTimeMillis);
}
