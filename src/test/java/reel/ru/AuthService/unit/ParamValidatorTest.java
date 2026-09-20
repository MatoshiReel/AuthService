package reel.ru.AuthService.unit;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reel.ru.AuthService.service.validation.ParamValidator;
import ru.reel.request.error.RequestParamError;
import ru.reel.request.error.reason.ErrorReason;

import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
public class ParamValidatorTest {
    @Autowired
    private ParamValidator<String> stringValidator;
    @Autowired
    private ParamValidator<Integer> integerValidator;

    @Test
    @DisplayName("Validate parameter of String type with null value.")
    public void paramValidatorOfStringNullValue() {
        RequestParamError error = stringValidator.validate(null, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getErrorReason(), Matchers.equalTo(ErrorReason.EMPTY));
    }

    @Test
    @DisplayName("Validate parameter of String type with not null value.")
    public void paramValidatorOfStringNotNullValue() {
        RequestParamError error = stringValidator.validate("not_null", "notnull");
        assertThat(error, Matchers.nullValue());
    }

    @Test
    @DisplayName("Validate parameter of Integer type with null value.")
    public void paramValidatorOfIntegerNullValue() {
        RequestParamError error = integerValidator.validate(null, null);
        assertThat(error, Matchers.notNullValue());
        assertThat(error.getErrorReason(), Matchers.equalTo(ErrorReason.EMPTY));
    }

    @Test
    @DisplayName("Validate parameter of Integer type with not null value.")
    public void paramValidatorOfIntegerNotNullValue() {
        RequestParamError error = integerValidator.validate(10, "notnull");
        assertThat(error, Matchers.nullValue());
    }
}
