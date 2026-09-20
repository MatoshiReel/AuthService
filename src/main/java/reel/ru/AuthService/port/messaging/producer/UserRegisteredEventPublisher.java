package reel.ru.AuthService.port.messaging.producer;

public interface UserRegisteredEventPublisher {
    void publish(String userId);
}
