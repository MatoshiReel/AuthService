package reel.ru.AuthService.service.error;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @see RequestError
 */
@Getter
@SuperBuilder
public class FieldRequestError extends RequestError {
    protected final String field;

    public FieldRequestError(String field, Reason reason, String message) {
        super(reason, message);
        this.field = field;
    }
}
