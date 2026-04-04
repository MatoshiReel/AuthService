package reel.ru.AuthService.converter;

import org.springframework.stereotype.Component;
import reel.ru.AuthService.dto.AccountDto;
import reel.ru.AuthService.entity.Account;

@Component
public class AccountConverter implements Converter<AccountDto, Account> {
    @Override
    public Account convert(AccountDto dto) {
        if(dto == null)
            return null;
        return new Account(dto.login, dto.password, null);
    }
}
