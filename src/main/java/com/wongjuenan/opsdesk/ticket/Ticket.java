package com.wongjuenan.opsdesk.ticket;

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
@Table(name = "ticket")
class Ticket {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 8000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false, length = 20)
    private TicketStatus status;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "assigned_to_user_id")
    private UUID assignedToUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected Ticket() {
    }

    Ticket(UUID workspaceId, String title, String description, UUID createdByUserId) {
        this.id = UUID.randomUUID();
        this.workspaceId = workspaceId;
        this.title = title;
        this.description = description;
        this.status = TicketStatus.OPEN;
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

    UUID id() {
        return id;
    }

    UUID workspaceId() {
        return workspaceId;
    }

    String title() {
        return title;
    }

    String description() {
        return description;
    }

    TicketStatus status() {
        return status;
    }

    UUID createdByUserId() {
        return createdByUserId;
    }

    UUID assignedToUserId() {
        return assignedToUserId;
    }

    Instant createdAt() {
        return createdAt;
    }

    Instant updatedAt() {
        return updatedAt;
    }

    void transitionTo(TicketStatus next) {
        this.status = next;
    }
}
