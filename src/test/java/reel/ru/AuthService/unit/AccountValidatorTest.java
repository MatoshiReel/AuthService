package reel.ru.AuthService.unit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reel.ru.AuthService.service.error.FieldRequestError;
import reel.ru.AuthService.service.error.Reason;
import reel.ru.AuthService.entity.Account;
import reel.ru.AuthService.repository.AccountRepository;
import reel.ru.AuthService.service.parser.GsonFactory;
import reel.ru.AuthService.service.security.encryption.EncoderFactory;
import reel.ru.AuthService.service.validation.AccountValidator;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
public class AccountValidatorTest {
    @Autowired
    private AccountValidator validator;
    @Autowired
    private GsonFactory gsonFactory;
    private static Account testAccount;
    private static final String testAccountPassword = "123456";

    @BeforeAll
    static void setup(@Autowired AccountRepository accountRepository) {
        testAccount = Account.builder().login("TestLogin").password(EncoderFactory.getArgon2Encoder().encode(testAccountPassword)).build();
        accountRepository.save(testAccount);
    }

    @Test
    @DisplayName("Validate account with null login.")
    public void validateAccountWithNullLogin() {
        Account account = Account.builder().login(null).build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.EMPTY));
        assertThat(error.getField(), Matchers.equalTo("login"));
    }

    @Test
    @DisplayName("Validate account with invalid pattern of login.")
    public void validateAccountWithInvalidPatternOfLogin() {
        Account account = Account.builder().login(" Lo-gin&^%$").build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.PATTERN));
        assertThat(error.getField(), Matchers.equalTo("login"));
    }

    @Test
    @DisplayName("Validate account with less size login.")
    public void validateAccountWithLessSizeLogin() {
        StringBuilder login = new StringBuilder();
        for(int i = 0; i < AccountValidator.LOGIN_MIN_SIZE-1; i++)
            login.append(i);
        Account account = Account.builder().login(login.toString()).build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.LESS_SIZE));
        assertThat(error.getField(), Matchers.equalTo("login"));
    }

    @Test
    @DisplayName("Validate account with greater size login.")
    public void validateAccountWithGreaterSizeLogin() {
        StringBuilder login = new StringBuilder();
        for(int i = 0; i <= AccountValidator.LOGIN_MAX_SIZE; i++)
            login.append(i);
        Account account = Account.builder().login(login.toString()).build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.GREATER_SIZE));
        assertThat(error.getField(), Matchers.equalTo("login"));
    }

    @Test
    @DisplayName("Validate account with valid login and null password.")
    public void validateAccountWithNullPassword() {
        Account account = Account.builder().login("12345").password(null).build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.EMPTY));
        assertThat(error.getField(), Matchers.equalTo("password"));
    }

    @Test
    @DisplayName("Validate account with valid login and invalid pattern of login.")
    public void validateAccountWithInvalidPatternOfPassword() {
        Account account = Account.builder().login("12345").password(" 24Lo-gin&^%$  _").build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.PATTERN));
        assertThat(error.getField(), Matchers.equalTo("password"));
    }

    @Test
    @DisplayName("Validate account with valid login and less size password.")
    public void validateAccountWithLessSizePassword() {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i < AccountValidator.PASSWORD_MIN_SIZE-1; i++)
            password.append(i);
        Account account = Account.builder().login("12345").password(password.toString()).build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.LESS_SIZE));
        assertThat(error.getField(), Matchers.equalTo("password"));
    }

    @Test
    @DisplayName("Validate account with valid login and greater size password.")
    public void validateAccountWithGreaterSizePassword() {
        StringBuilder password = new StringBuilder();
        for(int i = 0; i <= AccountValidator.PASSWORD_MAX_SIZE; i++)
            password.append(i);
        Account account = Account.builder().login("12345").password(password.toString()).build();
        FieldRequestError error = validator.validate(account, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.GREATER_SIZE));
        assertThat(error.getField(), Matchers.equalTo("password"));
    }

    @Test
    @DisplayName("Validate account with valid login, valid password and null repeated password for sign up mode.")
    public void validateAccountWithNullRepeatedPasswordForSignUpMode() {
        String json = "{\"login\":\"12345\",\"password\":\"1234567890\",\"repeatedPassword\":null}";
        Account account = gsonFactory.get().fromJson(json, Account.class);
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_UP);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.EMPTY));
        assertThat(error.getField(), Matchers.equalTo("repeatedPassword"));
    }

    @Test
    @DisplayName("Validate account with valid login, valid password and not matched repeated password for sign up mode.")
    public void validateAccountWithNotMatchRepeatedPasswordForSignUpMode() {
        String json = "{\"login\":\"12345\",\"password\":\"1234567890\",\"repeatedPassword\":\"0987654321\"}";
        Account account = gsonFactory.get().fromJson(json, Account.class);
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_UP);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.NOT_MATCH));
        assertThat(error.getField(), Matchers.equalTo("repeatedPassword"));
    }

    @Test
    @DisplayName("Validate account with valid login, valid password and valid repeated password, but existing login for sign up mode.")
    public void validateAccountWithExistingLoginForSignUpMode() {
        String json = String.format("{\"login\":\"%s\",\"password\":\"%s\",\"repeatedPassword\":\"%s\"}", testAccount.getLogin(), testAccountPassword, testAccountPassword);
        Account account = gsonFactory.get().fromJson(json, Account.class);
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_UP);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.EXISTS));
        assertThat(error.getField(), Matchers.equalTo("login"));
    }

    @Test
    @DisplayName("Validate account with valid login, valid password and valid repeated password for sign up mode.")
    public void validateAccountWithValidParametersForSignUpMode() {
        String json = String.format("{\"login\":\"%s\",\"password\":\"%s\",\"repeatedPassword\":\"%s\"}", String.format("%Sasdfb", testAccount.getLogin()), testAccountPassword, testAccountPassword);
        Account account = gsonFactory.get().fromJson(json, Account.class);
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_UP);
        assertThat(error, Matchers.nullValue());
    }

    @Test
    @DisplayName("Validate account with valid login and valid password, but login not exists for sign in mode.")
    public void validateAccountWithNotExistingLoginForSignInMode() {
        Account account = Account.builder().login(String.format("%Sasdfb", testAccount.getLogin())).password(testAccountPassword).build();
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_IN);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.NOT_EXISTS));
        assertThat(error.getField(), Matchers.equalTo("login"));
    }

    @Test
    @DisplayName("Validate account with valid login and valid password, but not matched password for sign in mode.")
    public void validateAccountWithNotMatchedPasswordForSignInMode() {
        Account account = Account.builder().login(testAccount.getLogin()).password(String.format("%Sasdfb", testAccountPassword)).build();
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_IN);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getReason(), Matchers.equalTo(Reason.NOT_MATCH));
        assertThat(error.getField(), Matchers.equalTo("password"));
    }

    @Test
    @DisplayName("Validate account with valid login and valid password for sign in mode.")
    public void validateAccountWithValidParametersForSignInMode() {
        Account account = Account.builder().login(testAccount.getLogin()).password(testAccountPassword).build();
        FieldRequestError error = validator.validate(account, AccountValidator.Mode.SIGN_IN);
        assertThat(error, Matchers.nullValue());
    }
}
