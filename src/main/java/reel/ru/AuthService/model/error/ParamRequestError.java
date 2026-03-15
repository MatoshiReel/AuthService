package reel.ru.AuthService.model.error;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @see RequestError
 */
@Getter
@SuperBuilder
public class ParamRequestError extends RequestError {
    protected final String param;

    public ParamRequestError(String param, Reason reason, String message) {
        super(reason, message);
        this.param = param;
    }
}
