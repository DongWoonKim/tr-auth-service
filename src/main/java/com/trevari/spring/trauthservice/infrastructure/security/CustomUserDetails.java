package com.trevari.spring.trauthservice.infrastructure.security;

import com.trevari.spring.trauthservice.domain.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String userId;
    private final String password;
    private final List<GrantedAuthority> authorities;

    // 👇 role을 하나만 꺼내주는 헬퍼 메서드
    public String getRole() {
        return authorities.stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");
    }

    public static CustomUserDetails from(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .userId(user.getUserId())
                .password(user.getPassword())
                .authorities( List.of (
                    new SimpleGrantedAuthority(user.getRole().name())
                ))
                .build();
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return userId; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
