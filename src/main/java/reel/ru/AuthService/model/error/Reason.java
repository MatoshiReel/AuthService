package reel.ru.AuthService.model.error;

/**
 * The {@code Reason} is an enum, that points on one of the reason of request error.
 */
public enum Reason {
    EMPTY,
    LESS_SIZE,
    GREATER_SIZE,
    EXISTS,
    NOT_EXISTS,
    NOT_MATCH,
    DECRYPTION
}
