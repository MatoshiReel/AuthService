package reel.ru.AuthService.api;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.encrypt.Encryptors;
import reel.ru.AuthService.service.error.Reason;
import reel.ru.AuthService.entity.Account;

import javax.crypto.KeyGenerator;

import java.security.NoSuchAlgorithmException;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
    @LocalServerPort
    private int port;
    @Value("${ENC_AES_SECRET_KEY}")
    private String secretKeyAES;
    @Value("${ENC_AES_SALT}")
    private String saltAES;
    private static final String testLogin = "TestLogin";
    private static final String testPassword = "123456";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://127.0.0.1";
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("auth/signin tests")
    class SignInTests {
        @Test
        @Order(1)
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with EMPTY body.")
        void sendPostAuthSignInRequestWithEmptyBody() {
            given()
                    .when()
                    .port(port)
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        @Order(2)
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with BAD ENCRYPTED body.")
        void sendPostAuthSignInRequestWithBadEncryptedBody() throws NoSuchAlgorithmException {
            given()
                    .body(Encryptors.delux(KeyGenerator.getInstance("AES").generateKey().toString(), saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", testLogin, testPassword)))
                    .when()
                    .port(port)
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.DECRYPTION.name()));
        }

        @Test
        @Order(3)
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with BAD JSON FORMAT body.")
        void sendPostAuthSignInRequestWithBadJsonBody() {
            given()
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt("{\"username\"=\"login\" \"password\":\"password\""))
                    .when()
                    .port(port)
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .log().body()
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.JSON_FORMAT.name()));
        }

        @Test
        @Order(4)
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with BAD JSON VALIDATING data in the body.")
        void sendPostAuthSignInRequestWithBadValidatingDataBody() {
            Account account = Account.builder().login("ab").password("12").build();
            given()
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", account.getLogin(), account.getPassword())))
                    .when()
                    .port(port)
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("field", Matchers.notNullValue());
        }

        @Test
        @Order(5)
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with VALID data and EXISTING login in the body.")
        void sendPostAuthSignInRequestWithValidDataAndExistingLoginBody() {
            given()
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", testLogin, testPassword)))
                    .when()
                    .port(port)
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(200);
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("auth/signup tests")
    class SignUpTests {
        @Test
        @Order(1)
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with EMPTY body.")
        void sendPostAuthSignUpRequestWithEmptyBody() {
            given()
                    .when()
                    .port(port)
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        @Order(2)
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with BAD ENCRYPTED body.")
        void sendPostAuthSignUpRequestWithBadEncryptedBody() throws NoSuchAlgorithmException {
            given()
                    .body(Encryptors.delux(KeyGenerator.getInstance("AES").generateKey().toString(), saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", testLogin, testPassword)))
                    .when()
                    .port(port)
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.DECRYPTION.name()));
        }

        @Test
        @Order(3)
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with BAD JSON FORMAT body.")
        void sendPostAuthSignUpRequestWithBadJsonBody() {
            given()
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt("{\"username\"=\"login\" \"password\":\"password\""))
                    .when()
                    .port(port)
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.JSON_FORMAT.name()));
        }

        @Test
        @Order(4)
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with BAD JSON VALIDATING data in the body.")
        void sendPostAuthSignUpRequestWithBadValidatingDataBody() {
            Account account = Account.builder().login("ab").password("12").build();
            given()
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", account.getLogin(), account.getPassword())))
                    .when()
                    .port(port)
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("field", Matchers.notNullValue());
        }

        @Test
        @Order(5)
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with VALID data and NOT EXISTING login in the body.")
        void sendPostAuthSignUpRequestWithValidDataAndNotExistingLoginBody() {
            given()
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\", \"repeatedPassword\":\"%s\"}", testLogin, testPassword, testPassword)))
                    .when()
                    .port(port)
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .log().body()
                    .statusCode(201);
        }
    }

    @Nested
    @DisplayName("auth/otp/verify tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class OtpVerifyTest {
        @Test
        @Order(1)
        @Tag("auth/otp/verify")
        @DisplayName("POST request /auth/otp/verify with EMPTY email param.")
        void sendPostAuthOtpVerifyRequestWithEmptyEmailParam() {
            given()
                    .when()
                    .queryParam("otp", "000000")
                    .port(port)
                    .post("/auth/otp/verify")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("param", Matchers.notNullValue())
                    .body("param", Matchers.equalTo("email"))
                    .body("reason", Matchers.equalTo(Reason.EMPTY.name()));
        }

        @Test
        @Order(2)
        @Tag("auth/otp/verify")
        @DisplayName("POST request /auth/otp/verify with EMPTY otp param.")
        void sendPostAuthOtpVerifyRequestWithEmptyOtpParam() {
            given()
                    .when()
                    .queryParam("email", "sdf@sdf.dg")
                    .port(port)
                    .post("/auth/otp/verify")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("param", Matchers.notNullValue())
                    .body("param", Matchers.equalTo("otp"))
                    .body("reason", Matchers.equalTo(Reason.EMPTY.name()));
        }

        @Test
        @Order(3)
        @Tag("auth/otp/verify")
        @DisplayName("POST request /auth/otp/verify with invalid params.")
        void sendPostAuthOtpVerifyRequestWithInvalidParams() {
            given()
                    .when()
                    .queryParam("email", "sdf@sdf.dg")
                    .queryParam("otp", "000000")
                    .port(port)
                    .post("/auth/otp/verify")
                    .then()
                    .assertThat()
                    .statusCode(403);
        }
    }
}