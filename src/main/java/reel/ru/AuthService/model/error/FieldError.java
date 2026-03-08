package reel.ru.AuthService.model.error;

/**
 * The {@code FieldError} is a DTO class, that describes a reason of Bad Request response status.
 * @param field points on the field which consists error.
 * @param reason one of a {@link Reason} enum value, that describes a reason of error.
 * @param message describes error.
 * @see Reason
 */
public record FieldError(String field, Reason reason, String message) {
    public static FieldErrorBuilder builder() {
        return new FieldErrorBuilder();
    }
}
