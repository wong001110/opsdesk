package com.wongjuenan.opsdesk.identity;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    DatabaseUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        AppUser user = users.findByEmailNormalized(IdentityDirectory.normalizeEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
        return new OpsDeskPrincipal(
                user.id(), user.emailNormalized(), user.passwordHash(), user.enabled());
    }
}
