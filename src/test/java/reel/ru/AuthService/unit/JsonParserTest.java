package reel.ru.AuthService.unit;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reel.ru.AuthService.model.jpa.entity.Account;
import reel.ru.AuthService.model.parser.JsonParser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class JsonParserTest {
    @Autowired
    private JsonParser<Account> jsonParser;

    @Test
    @DisplayName("Check is parsing from valid object of Account type to json string correct.")
    public void parseToJsonTest() throws JsonProcessingException {
        String json = "{\"id\":null,\"login\":\"login\",\"password\":\"password\",\"repeatedPassword\":null,\"email\":null,\"is2FaEnabled\":false,\"isSearchingHistoryEnabled\":false,\"isRecommendationEnabled\":false}";
        Account account = Account.builder().login("login").password("password").build();
        assertThat(jsonParser.parseToJson(account), equalTo(json));
    }

    @Test
    @DisplayName("Check is parsing from valid json string to object of Account type correct.")
    public void parseToObjectTest() throws JsonProcessingException {
        String json = "{\"login\":\"login12\",\"password\":\"password12\",\"email\":null}";
        Account account = jsonParser.parseToObject(json, Account.class);
        assertThat(account.getLogin(), equalTo("login12"));
        assertThat(account.getPassword(), equalTo("password12"));
    }

    @Test
    @DisplayName("Check is jsonParser throws JsonProcessingException if transfer invalid json string.")
    public void parseToObjectWithExceptionTest() {
        String json = "{\"AccountId\":null,\"login\":\"login12\"";
        assertThrows(JsonProcessingException.class, () -> {jsonParser.parseToObject(json, Account.class);});
    }
}
