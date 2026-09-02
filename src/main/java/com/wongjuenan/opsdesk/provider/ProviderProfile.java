package com.wongjuenan.opsdesk.provider;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "provider_profile")
class ProviderProfile {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "profile_name", nullable = false, length = 120)
    private String profileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false, length = 80)
    private ProviderType providerType;

    @Column(name = "base_url", nullable = false, length = 2048)
    private String trustedOrigin;

    @Column(name = "model_name", nullable = false, length = 200)
    private String model;

    @Column(name = "credential_reference", nullable = false, length = 500)
    private String credentialReference;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected ProviderProfile() {
    }

    ProviderProfile(
            UUID workspaceId,
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String model,
            String credentialReference,
            UUID createdByUserId) {
        this.id = UUID.randomUUID();
        this.workspaceId = workspaceId;
        this.profileName = profileName;
        this.providerType = providerType;
        this.trustedOrigin = trustedOrigin;
        this.model = model;
        this.credentialReference = credentialReference;
        this.createdByUserId = createdByUserId;
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

    void reconfigure(
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String model,
            String credentialReference) {
        this.profileName = profileName;
        this.providerType = providerType;
        this.trustedOrigin = trustedOrigin;
        this.model = model;
        this.credentialReference = credentialReference;
    }

    UUID id() {
        return id;
    }

    UUID workspaceId() {
        return workspaceId;
    }

    String profileName() {
        return profileName;
    }

    ProviderType providerType() {
        return providerType;
    }

    String trustedOrigin() {
        return trustedOrigin;
    }

    String model() {
        return model;
    }

    String credentialReference() {
        return credentialReference;
    }

    boolean credentialConfigured() {
        return credentialReference != null && !credentialReference.isBlank();
    }

    boolean enabled() {
        return enabled;
    }
}
