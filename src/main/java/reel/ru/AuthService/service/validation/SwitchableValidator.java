package reel.ru.AuthService.service.validation;
import reel.ru.AuthService.service.error.RequestError;

/**
 * The {@code SwitchableValidator} provides validation taking
 * into the passed validation mode.
 * @param <T> validated object.
 * @param <M> validation mode.
 * @param <R> returned error type {@link RequestError}, not null if an error was detected during validation process.
 * @see RequestError
 */
public interface SwitchableValidator<T, M, R extends RequestError> {
    R validate(T obj, M mode);
}
