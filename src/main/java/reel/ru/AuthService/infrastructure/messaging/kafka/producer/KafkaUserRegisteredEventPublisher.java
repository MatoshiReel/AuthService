package reel.ru.AuthService.infrastructure.messaging.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reel.ru.AuthService.port.messaging.producer.UserRegisteredEventPublisher;

@Component
public class KafkaUserRegisteredEventPublisher implements UserRegisteredEventPublisher {
    private final KafkaTemplate<String, String> template;

    public KafkaUserRegisteredEventPublisher(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public void publish(String userId) {
        template.send("user-registered", userId);
    }
}
