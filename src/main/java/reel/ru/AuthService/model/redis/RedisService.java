package reel.ru.AuthService.model.redis;

import jakarta.annotation.PreDestroy;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    private final JedisConnectionFactory connectionFactory;
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService() {
        this.connectionFactory = new JedisConnectionFactory(new RedisStandaloneConfiguration("localhost", 6379));
        this.connectionFactory.afterPropertiesSet();
        this.redisTemplate = new RedisTemplate<>();
        this.redisTemplate.setConnectionFactory(connectionFactory);
        this.redisTemplate.setDefaultSerializer(StringRedisSerializer.UTF_8);
        this.redisTemplate.afterPropertiesSet();
    }


    public RedisOperations<String, String> getRedisOperations() {
        return redisTemplate.opsForValue().getOperations();
    }

    @PreDestroy
    public void destroy() {
        this.connectionFactory.destroy();
    }
}
