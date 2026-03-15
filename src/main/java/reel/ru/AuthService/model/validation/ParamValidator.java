package reel.ru.AuthService.model.validation;

import org.springframework.stereotype.Component;
import reel.ru.AuthService.model.error.ErrorMessageFactory;
import reel.ru.AuthService.model.error.ParamRequestError;
import reel.ru.AuthService.model.error.Reason;

/**
 * @see Validator
 */
@Component
public class ParamValidator<T> implements Validator<T, ParamRequestError> {
    @Override
    public ParamRequestError validate(T param, String paramName) {
        if(isEmpty(param)) {
            return ParamRequestError.builder().param(paramName).reason(Reason.EMPTY).message(String.format(ErrorMessageFactory.get(Reason.EMPTY), paramName)).build();
        }
        return null;
    }

    public boolean isEmpty(T param) {
        return param == null;
    }
}
