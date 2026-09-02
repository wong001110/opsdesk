package com.wongjuenan.opsdesk.workspace;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
class WorkspaceMembershipId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected WorkspaceMembershipId() {
    }

    WorkspaceMembershipId(UUID workspaceId, UUID userId) {
        this.workspaceId = workspaceId;
        this.userId = userId;
    }

    UUID workspaceId() {
        return workspaceId;
    }

    UUID userId() {
        return userId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof WorkspaceMembershipId that)) {
            return false;
        }
        return Objects.equals(workspaceId, that.workspaceId)
                && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workspaceId, userId);
    }
}
