package com.wongjuenan.opsdesk.security;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public final class OpsDeskPrincipal implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID userId;
    private final String username;
    private String passwordHash;
    private final boolean enabled;

    public OpsDeskPrincipal(UUID userId, String username, String passwordHash, boolean enabled) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.enabled = enabled;
    }

    public UUID userId() {
        return userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
