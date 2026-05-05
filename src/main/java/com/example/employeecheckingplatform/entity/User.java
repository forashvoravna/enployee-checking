package com.example.employeecheckingplatform.entity;

import com.example.employeecheckingplatform.entity.AbstractEntity;
import com.example.employeecheckingplatform.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "user_session")
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "ignoreUnknown"}) // Xatolardan himoya
@Table(name = "users")
public class User extends AbstractEntity1 implements UserDetails {

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("branch_id")
    private Long branchId;

    @JsonProperty("rank_id")
    private Long rankId;

    private String gender;

    @Column(unique = true, nullable = false)
    private String jshshir;

    private String attachmentId;

    private String status;






    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {     // foydalanuvchi muddati o'tmaganligini bildiradi, true bo'lsa account faol holatda degani
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {      // foydalanuvchi bloklanmaganligini bildiradi, true bo'lsa account bloklanmagan holatda degani
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {     // Foydalanuvchining paroli (yoki boshqa autentifikatsiya ma’lumotlari) muddati o‘tmagan bo‘lsa true qaytaradi.
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {    // Foydalanuvchi akkaunti faol yoki yo‘qligini bildiradi. true bo‘lsa — akkaunt foydalanuvchi tomonidan yoki tizim tomonidan o‘chirib qo‘yilmagan.
        return true;
    }
}
