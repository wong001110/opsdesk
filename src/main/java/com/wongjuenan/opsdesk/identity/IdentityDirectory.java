package com.wongjuenan.opsdesk.identity;

import java.util.Locale;
import java.util.UUID;

import com.wongjuenan.opsdesk.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdentityDirectory {

    private final AppUserRepository users;

    public IdentityDirectory(AppUserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public UserIdentity requireByEmail(String email) {
        return users.findByEmailNormalized(normalizeEmail(email))
                .map(IdentityDirectory::toIdentity)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    @Transactional(readOnly = true)
    public UserIdentity requireById(UUID userId) {
        return users.findById(userId)
                .map(IdentityDirectory::toIdentity)
                .orElseThrow(() -> ApiException.notFound("User not found"));
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static UserIdentity toIdentity(AppUser user) {
        return new UserIdentity(user.id(), user.email(), user.displayName(), user.enabled());
    }

    public record UserIdentity(UUID id, String email, String displayName, boolean enabled) {
    }
}
