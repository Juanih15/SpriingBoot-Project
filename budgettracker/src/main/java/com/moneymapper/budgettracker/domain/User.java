package com.moneymapper.budgettracker.domain;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

/** Minimal user entity that plugs straight into Spring Security. */
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    /** BCrypt-hashed password. */
    @Column(nullable = false)
    private String password;

    private boolean enabled = true;

    protected User() {
    } // JPA only

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /* ------------ UserDetails ------------- */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /* getters if you need them elsewhere */
    public Long getId() {
        return id;
    }
}
