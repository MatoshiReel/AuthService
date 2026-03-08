package reel.ru.AuthService.model.error;

import lombok.NoArgsConstructor;

/**
 * @see FieldError
 */
@NoArgsConstructor
public class FieldErrorBuilder {
    private String field;
    private Reason reason;
    private String message;

    public FieldErrorBuilder field(String parameter) {
        this.field = parameter;
        return this;
    }

    public FieldErrorBuilder reason(Reason reason) {
        this.reason = reason;
        return this;
    }

    public FieldErrorBuilder message(String message) {
        this.message = message;
        return this;
    }

    public FieldError build() {
        return new FieldError(this.field, this.reason, this.message);
    }
}
