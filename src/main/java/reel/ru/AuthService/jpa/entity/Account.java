package reel.ru.AuthService.jpa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name="accounts")
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="login", nullable = false, unique = true, length=35)
    private String login;

    @Column(name="password", nullable = false, length=86)
    private String password;

    @Transient
    private String repeatedPassword;

    @Column(name="email", nullable = true, unique = true, length=255)
    private String email;

    @Column(name="2fa_enabled")
    private Boolean is2FaEnabled = false;

    @Column(name="searching_history_enabled")
    private Boolean isSearchingHistoryEnabled = false;

    @Column(name="recommendation_enabled")
    private Boolean isRecommendationEnabled = false;
}
