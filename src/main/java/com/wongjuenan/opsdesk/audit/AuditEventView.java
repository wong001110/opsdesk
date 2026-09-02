package com.wongjuenan.opsdesk.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEventView(
        UUID id,
        UUID workspaceId,
        UUID actorUserId,
        String action,
        String targetType,
        UUID targetId,
        AuditOutcome outcome,
        UUID correlationId,
        Instant occurredAt) {
}
