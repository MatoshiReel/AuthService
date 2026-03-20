package reel.ru.AuthService.service.validation;

import org.springframework.stereotype.Component;
import reel.ru.AuthService.service.error.FieldRequestError;
import reel.ru.AuthService.entity.Account;
import reel.ru.AuthService.repository.AccountRepository;
import reel.ru.AuthService.service.security.encryption.EncoderFactory;
import reel.ru.AuthService.service.error.ErrorMessageFactory;
import reel.ru.AuthService.service.error.Reason;

/**
 * @see SwitchableValidator
 */
@Component
public class AccountValidator implements SwitchableValidator<Account, AccountValidator.Mode, FieldRequestError> {
    private final AccountRepository accountRepository;
    public static final int LOGIN_MAX_SIZE = 35;
    public static final int LOGIN_MIN_SIZE = 3;
    public static final int PASSWORD_MAX_SIZE = 24;
    public static final int PASSWORD_MIN_SIZE = 6;
    public static final String allowLoginCharacters = "a-z A-Z 0-9 _";
    public static final String allowPasswordCharacters = "a-z A-Z 0-9 _ ! @ # $ % ^ & * ( ) - + =";

    public AccountValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public FieldRequestError validate(Account account, Mode mode) {
        FieldRequestError fieldRequestError;
        fieldRequestError = this.validateLogin(account.getLogin());
        if(fieldRequestError == null) fieldRequestError = this.validatePassword(account.getPassword());
        if(fieldRequestError == null && mode == Mode.SIGN_UP) fieldRequestError = this.validateRepeatedPassword(account.getPassword(), account.getRepeatedPassword());
        if(fieldRequestError == null) fieldRequestError = this.validateLoginExisting(account.getLogin(), mode);
        if(fieldRequestError == null && mode == Mode.SIGN_IN) fieldRequestError = this.validatePasswordMatching(account.getLogin(), account.getPassword());
        return fieldRequestError;
    }

    public boolean isEmpty(String parameter) {
        return parameter == null || parameter.isEmpty();
    }

    public boolean isMatch(String firstParameter, String secondParameter) {
        return firstParameter.equals(secondParameter);
    }

    public boolean isPatternMatch(String field, Field accountField) {
        return switch(accountField) {
            case Field.LOGIN -> field.matches("^\\w+$");
            case Field.PASSWORD -> field.matches("^[\\w!@#$%^&*()\\-+=]+$");
        };
    }

    public boolean isSizeGreaterThenMax(String parameter, Field accountField) {
        return switch(accountField) {
            case Field.LOGIN -> parameter.length() > LOGIN_MAX_SIZE;
            case Field.PASSWORD -> parameter.length() > PASSWORD_MAX_SIZE;
        };
    }

    public boolean isSizeLessThenMin(String parameter, Field accountField) {
        return switch(accountField) {
            case Field.LOGIN -> parameter.length() < LOGIN_MIN_SIZE;
            case Field.PASSWORD -> parameter.length() < PASSWORD_MIN_SIZE;
        };
    }

    public boolean isLoginExists(String login) {
        return accountRepository.existsByLogin(login);
    }

    public boolean isPasswordMatches(String login, String password) {
        Account account = accountRepository.findByLogin(login);
        if(account == null) return false;
        return EncoderFactory.getArgon2Encoder().matches(password, account.getPassword());
    }

    public enum Field {
        LOGIN,
        PASSWORD
    }

    public enum Mode {
        SIGN_IN,
        SIGN_UP
    }

    private FieldRequestError validateLogin(String login) {
        String field = "login";
        if(this.isEmpty(login)) {
            return FieldRequestError.builder().field(field).reason(Reason.EMPTY).message(String.format(ErrorMessageFactory.get(Reason.EMPTY), field)).build();
        } else if(!this.isPatternMatch(login, Field.LOGIN)) {
            return FieldRequestError.builder().field(field).reason(Reason.PATTERN).message(String.format(ErrorMessageFactory.get(Reason.PATTERN), field, allowLoginCharacters)).build();
        } else if(this.isSizeLessThenMin(login, Field.LOGIN)) {
            return FieldRequestError.builder().field(field).reason(Reason.LESS_SIZE).message(String.format(ErrorMessageFactory.get(Reason.LESS_SIZE), field, LOGIN_MIN_SIZE)).build();
        } else if(this.isSizeGreaterThenMax(login, Field.LOGIN)) {
            return FieldRequestError.builder().field(field).reason(Reason.GREATER_SIZE).message(String.format(ErrorMessageFactory.get(Reason.GREATER_SIZE), field, LOGIN_MAX_SIZE)).build();
        }
        return null;
    }

    private FieldRequestError validateLoginExisting(String login, Mode mode) {
        String field = "login";
        if(mode == Mode.SIGN_UP && this.isLoginExists(login)) {
            return FieldRequestError.builder().field(field).reason(Reason.EXISTS).message(String.format(ErrorMessageFactory.get(Reason.EXISTS), field)).build();
        } else if(mode == Mode.SIGN_IN && !this.isLoginExists(login)) {
            return FieldRequestError.builder().field(field).reason(Reason.NOT_EXISTS).message(String.format(ErrorMessageFactory.get(Reason.NOT_EXISTS), field)).build();
        }
        return null;
    }

    private FieldRequestError validatePassword(String password) {
        String field = "password";
        if(this.isEmpty(password)) {
            return FieldRequestError.builder().field(field).reason(Reason.EMPTY).message(String.format(ErrorMessageFactory.get(Reason.EMPTY), field)).build();
        } else if(!this.isPatternMatch(password, Field.PASSWORD)) {
            return FieldRequestError.builder().field(field).reason(Reason.PATTERN).message(String.format(ErrorMessageFactory.get(Reason.PATTERN), field, allowPasswordCharacters)).build();
        } else if(this.isSizeLessThenMin(password, Field.PASSWORD)) {
            return FieldRequestError.builder().field(field).reason(Reason.LESS_SIZE).message(String.format(ErrorMessageFactory.get(Reason.LESS_SIZE), field, PASSWORD_MIN_SIZE)).build();
        } else if(this.isSizeGreaterThenMax(password, Field.PASSWORD)) {
            return FieldRequestError.builder().field(field).reason(Reason.GREATER_SIZE).message(String.format(ErrorMessageFactory.get(Reason.GREATER_SIZE), field, PASSWORD_MAX_SIZE)).build();
        }
        return null;
    }

    private FieldRequestError validatePasswordMatching(String login, String password) {
        String field = "password";
        if(!this.isPasswordMatches(login, password)) {
            return FieldRequestError.builder().field(field).reason(Reason.NOT_MATCH).message(String.format(ErrorMessageFactory.get(Reason.NOT_MATCH), field)).build();
        }
        return null;
    }

    private FieldRequestError validateRepeatedPassword(String password, String repeatedPassword) {
        String field = "repeatedPassword";
        if(this.isEmpty(repeatedPassword)) {
            return FieldRequestError.builder().field(field).reason(Reason.EMPTY).message(String.format(ErrorMessageFactory.get(Reason.EMPTY), field)).build();
        } else if(!this.isMatch(password, repeatedPassword)) {
            return FieldRequestError.builder().field(field).reason(Reason.NOT_MATCH).message(String.format(ErrorMessageFactory.get(Reason.NOT_MATCH), field)).build();
        }
        return null;
    }
}