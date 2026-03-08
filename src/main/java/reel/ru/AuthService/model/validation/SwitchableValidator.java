package reel.ru.AuthService.model.validation;

import reel.ru.AuthService.model.error.FieldError;

/**
 * The {@code SwitchableValidator} provides validation taking
 * into the passed validation mode.
 * @param <T> validated object.
 * @param <M> validation mode.
 */
public interface SwitchableValidator<T, M> {
    FieldError validate(T obj, M mode);
}
