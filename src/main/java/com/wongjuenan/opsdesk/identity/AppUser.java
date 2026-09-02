package com.wongjuenan.opsdesk.identity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "app_user")
class AppUser {

    @Id
    private UUID id;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "email_normalized", nullable = false, unique = true, length = 320)
    private String emailNormalized;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected AppUser() {
    }

    AppUser(String email, String emailNormalized, String passwordHash, String displayName) {
        this.id = UUID.randomUUID();
        this.email = email;
        this.emailNormalized = emailNormalized;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    UUID id() {
        return id;
    }

    String email() {
        return email;
    }

    String emailNormalized() {
        return emailNormalized;
    }

    String passwordHash() {
        return passwordHash;
    }

    String displayName() {
        return displayName;
    }

    boolean enabled() {
        return enabled;
    }
}
