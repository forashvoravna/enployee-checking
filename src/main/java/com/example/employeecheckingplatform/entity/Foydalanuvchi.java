package com.example.employeecheckingplatform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "foydalanuvchi", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Foydalanuvchi implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String toliqIsm;
    @Column(nullable = false)
    private String username;         // JShShIR
    private String parol;
//    @Lob
//    @Column(columnDefinition = "BYTEA")
//    private byte[] avatar;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role roli; // ADMIN, USER

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tashkilot_id", nullable = false)
    private Tashkilot tashkilot;

    @Column(nullable = false)
    private Boolean faol = true;

    // UserDetails
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> "ROLE_" + roli.name());
    }

    @Override
    public String getPassword() {
        return parol;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return faol;
    }
}
