package reel.ru.AuthService.service.error;

/**
 * The {@code Reason} is an enum, that points on one of the reason of 4xx response status.
 */
public enum Reason {
    EMPTY,
    PATTERN,
    LESS_SIZE,
    GREATER_SIZE,
    EXISTS,
    NOT_EXISTS,
    NOT_MATCH,
    DECRYPTION,
    JSON_FORMAT
}
