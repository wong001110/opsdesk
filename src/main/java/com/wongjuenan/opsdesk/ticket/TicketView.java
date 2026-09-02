package com.wongjuenan.opsdesk.ticket;

import java.time.Instant;
import java.util.UUID;

public record TicketView(
        UUID id,
        UUID workspaceId,
        String title,
        String description,
        TicketStatus status,
        UUID createdByUserId,
        UUID assignedToUserId,
        Instant createdAt,
        Instant updatedAt) {
}
