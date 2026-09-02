package com.wongjuenan.opsdesk.identity;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
class LocalIdentityBootstrap implements ApplicationRunner {

    private final BootstrapProperties properties;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    LocalIdentityBootstrap(
            BootstrapProperties properties,
            AppUserRepository users,
            PasswordEncoder passwordEncoder) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isEnabled()) {
            return;
        }

        String normalizedEmail = IdentityDirectory.normalizeEmail(properties.getEmail());
        if (!StringUtils.hasText(normalizedEmail)
                || !StringUtils.hasText(properties.getDisplayName())
                || !StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException(
                    "Local bootstrap requires email, display name, and password when enabled");
        }

        users.findByEmailNormalized(normalizedEmail).orElseGet(() -> users.save(new AppUser(
                properties.getEmail().trim(),
                normalizedEmail,
                passwordEncoder.encode(properties.getPassword()),
                properties.getDisplayName().trim())));
    }
}
