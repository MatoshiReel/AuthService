package reel.ru.AuthService.model.validation;

import org.springframework.stereotype.Component;
import reel.ru.AuthService.jpa.entity.Account;
import reel.ru.AuthService.jpa.repository.AccountRepository;
import reel.ru.AuthService.model.error.ErrorMessageFactory;
import reel.ru.AuthService.model.error.FieldError;
import reel.ru.AuthService.model.error.Reason;

/**
 * @see SwitchableValidator
 */
@Component
public class AccountValidator implements SwitchableValidator<Account, AccountValidator.Mode> {
    private static final int LOGIN_MAX_SIZE = 35;
    private static final int LOGIN_MIN_SIZE = 3;
    private static final int PASSWORD_MAX_SIZE = 24;
    private static final int PASSWORD_MIN_SIZE = 6;

    @Override
    public FieldError validate(Account account, Mode mode) {
        FieldError fieldError;
        fieldError = this.validateLogin(account.getLogin());
        if(fieldError == null) fieldError = this.validatePassword(account.getPassword());
        if(fieldError == null && mode == Mode.SIGN_UP) fieldError = this.validateRepeatedPassword(account.getPassword(), account.getRepeatedPassword());
        return fieldError;
    }

    public boolean isEmpty(String parameter) {
        return parameter == null || parameter.isEmpty();
    }

    public boolean isMatch(String firstParameter, String secondParameter) {
        return firstParameter.equals(secondParameter);
    }

    public boolean isSizeGreaterThenMax(String parameter, Parameter accountParameter) {
        return switch(accountParameter) {
            case Parameter.LOGIN -> parameter.length() > LOGIN_MAX_SIZE;
            case Parameter.PASSWORD -> parameter.length() > PASSWORD_MAX_SIZE;
        };
    }

    public boolean isSizeLessThenMin(String parameter, Parameter accountParameter) {
        return switch(accountParameter) {
            case Parameter.LOGIN -> parameter.length() < LOGIN_MIN_SIZE;
            case Parameter.PASSWORD -> parameter.length() < PASSWORD_MIN_SIZE;
        };
    }

    public boolean isLoginExists(String login, AccountRepository accountRepository) {
        return true;
    }

    public boolean isPasswordMatches(String login, String password, AccountRepository accountRepository) {
        return false;
    }

    public enum Parameter {
        LOGIN,
        PASSWORD
    }

    public enum Mode {
        SIGN_IN,
        SIGN_UP
    }

    private FieldError validateLogin(String login) {
        String field = "login";
        if(this.isEmpty(login)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.EMPTY)
                    .message(String.format(ErrorMessageFactory.get(Reason.EMPTY), field))
                    .build();
        } else if(this.isSizeLessThenMin(login, Parameter.LOGIN)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.LESS_SIZE)
                    .message(String.format(ErrorMessageFactory.get(Reason.LESS_SIZE), field, LOGIN_MIN_SIZE))
                    .build();
        } else if(this.isSizeGreaterThenMax(login, Parameter.LOGIN)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.GREATER_SIZE)
                    .message(String.format(ErrorMessageFactory.get(Reason.GREATER_SIZE), field, LOGIN_MAX_SIZE))
                    .build();
        }
        return null;
    }

    private FieldError validatePassword(String password) {
        String field = "password";
        if(this.isEmpty(password)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.EMPTY)
                    .message(String.format(ErrorMessageFactory.get(Reason.EMPTY), field))
                    .build();
        } else if(this.isSizeLessThenMin(password, Parameter.PASSWORD)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.LESS_SIZE)
                    .message(String.format(ErrorMessageFactory.get(Reason.LESS_SIZE), field, PASSWORD_MIN_SIZE))
                    .build();
        } else if(this.isSizeGreaterThenMax(password, Parameter.PASSWORD)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.GREATER_SIZE)
                    .message(String.format(ErrorMessageFactory.get(Reason.GREATER_SIZE), field, PASSWORD_MAX_SIZE))
                    .build();
        }
        return null;
    }

    private FieldError validateRepeatedPassword(String password, String repeatedPassword) {
        String field = "repeatedPassword";
        if(this.isEmpty(repeatedPassword)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.EMPTY)
                    .message(String.format(ErrorMessageFactory.get(Reason.EMPTY), field))
                    .build();
        } else if(!this.isMatch(password, repeatedPassword)) {
            return FieldError.builder()
                    .field(field)
                    .reason(Reason.NOT_MATCH)
                    .message(String.format(ErrorMessageFactory.get(Reason.NOT_MATCH), field))
                    .build();
        }
        return null;
    }
}
