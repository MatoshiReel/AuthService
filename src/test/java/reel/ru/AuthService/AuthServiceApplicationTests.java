package reel.ru.AuthService;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest
class AuthServiceApplicationTests {
	@BeforeEach
	void setup() {
		RestAssured.baseURI = "http://127.0.0.1";
		RestAssured.port = 8081;
	}

	@Test
	void contextLoads() {
		given().when().request("POST", "/auth/signin").then().assertThat().statusCode(404);
	}

}
