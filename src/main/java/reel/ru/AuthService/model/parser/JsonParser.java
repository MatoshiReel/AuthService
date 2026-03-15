package reel.ru.AuthService.model.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonParser<T> {
    private final ObjectMapper mapper = new ObjectMapper();

    public String parseToJson(T obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public T parseToObject(String jsonData, Class<T> serializedClass) throws JsonProcessingException {
        return mapper.readValue(jsonData, serializedClass);
    }
}
