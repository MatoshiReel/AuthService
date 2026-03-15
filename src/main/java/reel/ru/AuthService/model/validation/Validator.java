package reel.ru.AuthService.model.validation;

import reel.ru.AuthService.model.error.RequestError;

/**
 * @param <T> validated object.
 * @param <R> returned error type {@link RequestError}, not null if an error was detected during validation process.
 * @see RequestError
 */
public interface Validator<T, R extends RequestError> {
    R validate(T obj, String objName);
}
