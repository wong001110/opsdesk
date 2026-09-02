package com.wongjuenan.opsdesk.audit;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_event")
class AuditEvent {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(nullable = false, length = 120)
    private String action;

    @Column(name = "target_type", nullable = false, length = 80)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuditOutcome outcome;

    @Column(name = "correlation_id")
    private UUID correlationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected AuditEvent() {
    }

    AuditEvent(
            UUID workspaceId,
            UUID actorUserId,
            String action,
            String targetType,
            UUID targetId,
            AuditOutcome outcome) {
        this.id = UUID.randomUUID();
        this.workspaceId = workspaceId;
        this.actorUserId = actorUserId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.outcome = outcome;
    }

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }

    UUID id() {
        return id;
    }

    UUID workspaceId() {
        return workspaceId;
    }

    UUID actorUserId() {
        return actorUserId;
    }

    String action() {
        return action;
    }

    String targetType() {
        return targetType;
    }

    UUID targetId() {
        return targetId;
    }

    AuditOutcome outcome() {
        return outcome;
    }

    UUID correlationId() {
        return correlationId;
    }

    Instant occurredAt() {
        return occurredAt;
    }
}
