package reel.ru.AuthService.service.validation;

import ru.reel.request.error.RequestError;

/**
 * @param <T> validated object.
 * @param <R> returned error type {@link RequestError}, not null if an error was detected during validation process.
 */
public interface Validator<T, R extends RequestError> {
    R validate(T obj, String objName);
}
