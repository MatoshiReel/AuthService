package reel.ru.AuthService.model.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

@Component
public class GsonFactory {
    private final Gson gson;

    public GsonFactory() {
        GsonBuilder builder = new GsonBuilder();
        this.gson = builder.create();
    }

    public Gson get() {
        return this.gson;
    }
}
