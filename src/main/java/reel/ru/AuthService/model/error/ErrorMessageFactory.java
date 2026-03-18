package reel.ru.AuthService.model.error;

/**
 * The {@code ErrorMessageFactory} is a factory class, that standardized messages for {@link RequestError} message field.
 * @see RequestError
 */
public class ErrorMessageFactory {
    /**
     * @param reason one of a {@link Reason} enum value.
     * @return unformatted {@link String} message for {@link RequestError}.
     * @see Reason
     * @see RequestError
     */
    public static String get(Reason reason) {
        return switch(reason) {
            case Reason.EMPTY -> "%s is empty or null.";
            case Reason.LESS_SIZE -> "%s size is less then min possible size %d.";
            case Reason.GREATER_SIZE -> "%s size is greater then max possible size %d.";
            case Reason.EXISTS -> "%s is already exist.";
            case Reason.NOT_EXISTS -> "%s is not exist.";
            case Reason.NOT_MATCH -> "%s is not match.";
            case Reason.DECRYPTION -> "Encrypted data is not valid.";
            case Reason.JSON_FORMAT -> "JSON format or data is not valid.";
        };
    }
}
