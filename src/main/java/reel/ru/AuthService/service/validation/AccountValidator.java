package reel.ru.AuthService.service.validation;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;
import reel.ru.AuthService.dto.AccountDto;
import reel.ru.AuthService.entity.Account;
import reel.ru.AuthService.repository.AccountRepository;
import ru.reel.request.error.RequestFieldError;
import ru.reel.request.error.reason.ErrorReason;

/**
 * @see SwitchableValidator
 */
@Component
public class AccountValidator implements SwitchableValidator<AccountDto, AccountValidator.Mode, RequestFieldError> {
    private final AccountRepository accountRepository;
    private final Argon2PasswordEncoder argon2PasswordEncoder;
    public static final int LOGIN_MAX_SIZE = 20;
    public static final int LOGIN_MIN_SIZE = 3;
    public static final int PASSWORD_MAX_SIZE = 24;
    public static final int PASSWORD_MIN_SIZE = 6;
    public static final String allowLoginCharacters = "a-z A-Z 0-9 _";
    public static final String allowPasswordCharacters = "a-z A-Z 0-9 _ ! @ # $ % ^ & * ( ) - + =";

    public AccountValidator(AccountRepository accountRepository, Argon2PasswordEncoder argon2PasswordEncoder) {
        this.accountRepository = accountRepository;
        this.argon2PasswordEncoder = argon2PasswordEncoder;
    }

    @Override
    public RequestFieldError validate(AccountDto accountDto, Mode mode) {
        RequestFieldError error;
        error = this.validateLogin(accountDto.login);
        if(error == null) error = this.validatePassword(accountDto.password);
        if(error == null && mode == Mode.SIGN_UP) error = this.validateRepeatedPassword(accountDto.password, accountDto.repeatedPassword);
        if(error == null) error = this.validateLoginExisting(accountDto.login, mode);
        if(error == null && mode == Mode.SIGN_IN) error = this.validatePasswordMatching(accountDto.login, accountDto.password);
        return error;
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
        return argon2PasswordEncoder.matches(password, account.getPassword());
    }

    public enum Field {
        LOGIN,
        PASSWORD
    }

    public enum Mode {
        SIGN_IN,
        SIGN_UP
    }

    private RequestFieldError validateLogin(String login) {
        String field = "login";
        if(this.isEmpty(login)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.EMPTY).message(field).build();
        } else if(!this.isPatternMatch(login, Field.LOGIN)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.PATTERN).message(field, allowLoginCharacters).build();
        } else if(this.isSizeLessThenMin(login, Field.LOGIN)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.LESS_SIZE).message(field, String.valueOf(LOGIN_MIN_SIZE)).build();
        } else if(this.isSizeGreaterThenMax(login, Field.LOGIN)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.GREATER_SIZE).message(field, String.valueOf(LOGIN_MAX_SIZE)).build();
        }
        return null;
    }

    private RequestFieldError validateLoginExisting(String login, Mode mode) {
        String field = "login";
        if(mode == Mode.SIGN_UP && this.isLoginExists(login)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.EXIST).message(field).build();
        } else if(mode == Mode.SIGN_IN && !this.isLoginExists(login)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.NOT_EXIST).message(field).build();
        }
        return null;
    }

    private RequestFieldError validatePassword(String password) {
        String field = "password";
        if(this.isEmpty(password)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.EMPTY).message(field).build();
        } else if(!this.isPatternMatch(password, Field.PASSWORD)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.PATTERN).message(field, allowPasswordCharacters).build();
        } else if(this.isSizeLessThenMin(password, Field.PASSWORD)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.LESS_SIZE).message(field, String.valueOf(PASSWORD_MIN_SIZE)).build();
        } else if(this.isSizeGreaterThenMax(password, Field.PASSWORD)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.GREATER_SIZE).message(field, String.valueOf(PASSWORD_MAX_SIZE)).build();
        }
        return null;
    }

    private RequestFieldError validatePasswordMatching(String login, String password) {
        String field = "password";
        if(!this.isPasswordMatches(login, password)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.NOT_MATCH).message(field).build();
        }
        return null;
    }

    private RequestFieldError validateRepeatedPassword(String password, String repeatedPassword) {
        String field = "repeatedPassword";
        if(this.isEmpty(repeatedPassword)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.EMPTY).message(field).build();
        } else if(!this.isMatch(password, repeatedPassword)) {
            return RequestFieldError.builder().field(field).errorReason(ErrorReason.NOT_MATCH).message(field).build();
        }
        return null;
    }
}