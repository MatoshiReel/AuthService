package reel.ru.AuthService.api;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AuthControllerTests {
    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://127.0.0.1";
        RestAssured.port = 8081;
    }

    @Test
    @DisplayName("")
    void testName() {}
}
