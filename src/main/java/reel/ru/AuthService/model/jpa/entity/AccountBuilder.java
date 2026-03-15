package reel.ru.AuthService.model.jpa.entity;

public class AccountBuilder {
    private String login;
    private String password;
    private String email;

    public AccountBuilder login(String login) {
        this.login = login;
        return this;
    }

    public AccountBuilder password(String password) {
        this.password = password;
        return this;
    }

    public AccountBuilder email(String email) {
        this.email = email;
        return this;
    }

    public Account build() {
        return new Account(this.login, this.password, this.email);
    }
}
