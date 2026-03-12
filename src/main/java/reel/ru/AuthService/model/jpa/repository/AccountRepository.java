package reel.ru.AuthService.model.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import reel.ru.AuthService.model.jpa.entity.Account;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    boolean existsByLogin(String login);
    Account findByLogin(String login);
}
