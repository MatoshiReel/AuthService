package reel.ru.AuthService.service.validation;

import org.springframework.stereotype.Component;
import ru.reel.request.error.RequestParamError;
import ru.reel.request.error.reason.ErrorReason;

/**
 * @see Validator
 */
@Component
public class ParamValidator<T> implements Validator<T, RequestParamError> {
    @Override
    public RequestParamError validate(T param, String paramName) {
        if(isEmpty(param)) {
            return RequestParamError.builder()
                                    .param(paramName)
                                    .errorReason(ErrorReason.EMPTY)
                                    .message(paramName)
                                    .build();
        }
        return null;
    }

    public boolean isEmpty(T param) {
        return param == null;
    }
}
