package reel.ru.AuthService.model.security.token;

public interface JwtCreator {
    String create(String id, long expiredTimeMillis);
}
