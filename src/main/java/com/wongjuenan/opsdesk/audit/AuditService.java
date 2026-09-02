package com.wongjuenan.opsdesk.audit;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.wongjuenan.opsdesk.workspace.WorkspaceAccess;
import com.wongjuenan.opsdesk.workspace.WorkspaceRole;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuditService {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditEventRepository events;
    private final WorkspaceAccess workspaceAccess;

    public AuditService(AuditEventRepository events, WorkspaceAccess workspaceAccess) {
        this.events = events;
        this.workspaceAccess = workspaceAccess;
    }

    @Transactional
    public void record(
            UUID workspaceId,
            UUID actorUserId,
            String action,
            String targetType,
            UUID targetId,
            AuditOutcome outcome) {
        Objects.requireNonNull(workspaceId, "workspaceId");
        Objects.requireNonNull(actorUserId, "actorUserId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(outcome, "outcome");
        events.save(new AuditEvent(
                workspaceId,
                actorUserId,
                requireToken(action, "action", 120),
                requireToken(targetType, "targetType", 80),
                targetId,
                outcome));
    }

    @Transactional(readOnly = true)
    public List<AuditEventView> list(UUID workspaceId, UUID actorUserId, int requestedLimit) {
        workspaceAccess.requireAnyRole(
                workspaceId, actorUserId, WorkspaceRole.MANAGER, WorkspaceRole.ADMIN);
        int limit = Math.max(1, Math.min(requestedLimit, MAX_PAGE_SIZE));
        return events.findByWorkspaceIdOrderByOccurredAtDesc(workspaceId, PageRequest.of(0, limit)).stream()
                .map(AuditService::toView)
                .toList();
    }

    private static String requireToken(String value, String name, int maxLength) {
        if (!StringUtils.hasText(value) || value.length() > maxLength) {
            throw new IllegalArgumentException(name + " must contain text up to " + maxLength + " characters");
        }
        return value.trim();
    }

    private static AuditEventView toView(AuditEvent event) {
        return new AuditEventView(
                event.id(),
                event.workspaceId(),
                event.actorUserId(),
                event.action(),
                event.targetType(),
                event.targetId(),
                event.outcome(),
                event.correlationId(),
                event.occurredAt());
    }
}
