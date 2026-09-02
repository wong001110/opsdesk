package com.wongjuenan.opsdesk.workspace;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "workspace_membership")
class WorkspaceMembership {

    @EmbeddedId
    private WorkspaceMembershipId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_role", nullable = false, length = 20)
    private WorkspaceRole role;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected WorkspaceMembership() {
    }

    WorkspaceMembership(UUID workspaceId, UUID userId, WorkspaceRole role) {
        this.id = new WorkspaceMembershipId(workspaceId, userId);
        this.role = role;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        joinedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    UUID workspaceId() {
        return id.workspaceId();
    }

    UUID userId() {
        return id.userId();
    }

    WorkspaceRole role() {
        return role;
    }

    boolean active() {
        return active;
    }

    void changeRole(WorkspaceRole role) {
        this.role = role;
    }

    void reactivate(WorkspaceRole role) {
        this.role = role;
        this.active = true;
    }

    void deactivate() {
        this.active = false;
    }
}
