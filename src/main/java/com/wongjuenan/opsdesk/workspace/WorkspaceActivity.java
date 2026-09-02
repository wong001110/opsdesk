package com.wongjuenan.opsdesk.workspace;

import java.util.UUID;

public record WorkspaceActivity(
        Action action,
        UUID workspaceId,
        UUID actorUserId,
        UUID targetUserId) {

    public enum Action {
        WORKSPACE_CREATED,
        MEMBERSHIP_ADDED,
        MEMBERSHIP_REACTIVATED,
        MEMBERSHIP_ROLE_CHANGED,
        MEMBERSHIP_DEACTIVATED
    }
}
