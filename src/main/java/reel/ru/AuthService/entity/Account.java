package reel.ru.AuthService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter
@Entity
@Table(name="accounts")
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Setter
    @Column(name="login", nullable = false, unique = true, length=35)
    private String login;

    @Setter
    @Column(name="password", nullable = false, length=97)
    private String password;

    @Column(name="email", unique = true, length=255)
    private String email;

    @Column(name = "created_at", nullable = false)
    private final Date createdAt = new Date();

    @Column(name="2fa_enabled")
    private final Boolean is2FaEnabled = false;

    @Column(name="searching_history_enabled")
    private final Boolean isSearchingHistoryEnabled = false;

    @Column(name="recommendation_enabled")
    private final Boolean isRecommendationEnabled = false;

    @Setter
    @ManyToOne
    private Role role;

    public Account(String login, String password, String email) {
        this.login = login;
        this.password = password;
        this.email = email;
    }
}
