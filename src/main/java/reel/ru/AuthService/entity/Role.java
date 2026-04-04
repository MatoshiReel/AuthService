package reel.ru.AuthService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Entity
@Table(name = "roles")
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", length = 15, nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "role")
    private final Set<Account> accounts = new HashSet<>();

    public Role(String name) {
        this.name = name;
    }
}
