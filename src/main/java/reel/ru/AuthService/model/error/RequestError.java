package reel.ru.AuthService.model.error;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * The {@code RequestError} is a DTO class, that describes a reason of 4xx response status.
 * @see Reason
 */
@Getter
@SuperBuilder
public class RequestError {
    protected final Reason reason;
    protected final String message;

    public RequestError(Reason reason, String message) {
        this.reason = reason;
        this.message = message;
    }
}
