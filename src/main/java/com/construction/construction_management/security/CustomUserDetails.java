package com.construction.construction_management.security;

import com.construction.construction_management.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /** Login id is the phone number. */
    @Override
    public String getUsername() {
        return user.getPhone();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    /** An account is usable only after its phone number was verified by OTP. */
    @Override public boolean isEnabled() { return user.isPhoneVerified(); }
}
