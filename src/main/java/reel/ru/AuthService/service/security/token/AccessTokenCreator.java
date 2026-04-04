package reel.ru.AuthService.service.security.token;

public interface AccessTokenCreator {
    String create(String id, String role, long expiredTimeMillis);
}
