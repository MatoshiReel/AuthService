package reel.ru.AuthService.api;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.encrypt.Encryptors;
import reel.ru.AuthService.model.error.Reason;
import reel.ru.AuthService.model.jpa.entity.Account;
import reel.ru.AuthService.model.jpa.repository.AccountRepository;

import javax.crypto.KeyGenerator;

import java.security.NoSuchAlgorithmException;

import static io.restassured.RestAssured.given;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
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
    static void setup(@Autowired AccountRepository accountRepository) {
        RestAssured.baseURI = "http://127.0.0.1";
    }

    @Nested
    @DisplayName("auth/signin tests")
    class SignInTests {
        @Test
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with EMPTY body.")
        void sendPostAuthSignInRequestWithEmptyBody() {
            given()
                    .port(port)
                    .when()
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with BAD ENCRYPTED body.")
        void sendPostAuthSignInRequestWithBadEncryptedBody() throws NoSuchAlgorithmException {
            given()
                    .port(port)
                    .body(Encryptors.delux(KeyGenerator.getInstance("AES").generateKey().toString(), saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", testLogin, testPassword)))
                    .when()
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.DECRYPTION.name()));
        }

        @Test
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with BAD JSON FORMAT body.")
        void sendPostAuthSignInRequestWithBadJsonBody() {
            given()
                    .port(port)
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt("{\"username\":\"login\", \"password\":\"password\"}"))
                    .when()
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.JSON_FORMAT.name()));
        }

        @Test
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with BAD JSON VALIDATING data in the body.")
        void sendPostAuthSignInRequestWithBadValidatingDataBody() {
            Account account = Account.builder().login("ab").password("12").build();
            given()
                    .port(port)
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", account.getLogin(), account.getPassword())))
                    .when()
                    .post("/auth/signin")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("field", Matchers.notNullValue());
        }

        @Test
        @Tag("auth/signin")
        @DisplayName("POST request /auth/signin with VALID data and EXISTING login in the body.")
        void sendPostAuthSignInRequestWithValidDataAndExistingLoginBody() {
            given()
                    .port(port)
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", testLogin, testPassword)))
                    .when()
                    .post("/auth/signin")
                    .then()
                    .log().body()
                    .assertThat()
                    .statusCode(201);
        }
    }

    @Nested
    @DisplayName("auth/signup tests")
    class SignUpTests {
        @Test
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with EMPTY body.")
        void sendPostAuthSignUpRequestWithEmptyBody() {
            given()
                    .port(port)
                    .when()
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400);
        }

        @Test
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with BAD ENCRYPTED body.")
        void sendPostAuthSignUpRequestWithBadEncryptedBody() throws NoSuchAlgorithmException {
            given()
                    .port(port)
                    .body(Encryptors.delux(KeyGenerator.getInstance("AES").generateKey().toString(), saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", testLogin, testPassword)))
                    .when()
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.DECRYPTION.name()));
        }

        @Test
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with BAD JSON FORMAT body.")
        void sendPostAuthSignUpRequestWithBadJsonBody() {
            given()
                    .port(port)
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt("{\"username\":\"login\", \"password\":\"password\"}"))
                    .when()
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("reason", Matchers.equalTo(Reason.JSON_FORMAT.name()));
        }

        @Test
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with BAD JSON VALIDATING data in the body.")
        void sendPostAuthSignUpRequestWithBadValidatingDataBody() {
            Account account = Account.builder().login("ab").password("12").build();
            given()
                    .port(port)
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\"}", account.getLogin(), account.getPassword())))
                    .when()
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .statusCode(400)
                    .assertThat()
                    .body("field", Matchers.notNullValue());
        }

        @Test
        @Tag("auth/signup")
        @DisplayName("POST request /auth/signup with VALID data and NOT EXISTING login in the body.")
        void sendPostAuthSignUpRequestWithValidDataAndNotExistingLoginBody() {
            given()
                    .port(port)
                    .body(Encryptors.delux(secretKeyAES, saltAES).encrypt(String.format("{\"login\":\"%s\", \"password\":\"%s\", \"repeatedPassword\":\"%s\"}", testLogin, testPassword, testPassword)))
                    .when()
                    .post("/auth/signup")
                    .then()
                    .assertThat()
                    .log().body()
                    .statusCode(201);
        }
    }

    @Nested
    @DisplayName("auth/otp/verify tests")
    class OtpVerifyTest {
        @Test
        @Tag("auth/otp/verify")
        @DisplayName("POST request /auth/otp/verify with EMPTY email param.")
        void sendPostAuthOtpVerifyRequestWithEmptyEmailParam() {
            given()
                    .port(port)
                    .when()
                    .queryParam("otp", "000000")
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
        @Tag("auth/otp/verify")
        @DisplayName("POST request /auth/otp/verify with EMPTY otp param.")
        void sendPostAuthOtpVerifyRequestWithEmptyOtpParam() {
            given()
                    .port(port)
                    .when()
                    .queryParam("email", "sdf@sdf.dg")
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
        @Tag("auth/otp/verify")
        @DisplayName("POST request /auth/otp/verify with invalid params.")
        void sendPostAuthOtpVerifyRequestWithInvalidParams() {
            given()
                    .port(port)
                    .when()
                    .queryParam("email", "sdf@sdf.dg")
                    .queryParam("otp", "000000")
                    .post("/auth/otp/verify")
                    .then()
                    .assertThat()
                    .statusCode(403);
        }
    }
}
