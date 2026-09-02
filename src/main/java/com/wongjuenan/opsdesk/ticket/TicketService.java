package com.wongjuenan.opsdesk.ticket;

import java.util.List;
import java.util.UUID;

import com.wongjuenan.opsdesk.audit.AuditOutcome;
import com.wongjuenan.opsdesk.audit.AuditService;
import com.wongjuenan.opsdesk.common.ApiException;
import com.wongjuenan.opsdesk.workspace.WorkspaceAccess;
import com.wongjuenan.opsdesk.workspace.WorkspaceRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
class TicketService {

    private final TicketRepository tickets;
    private final WorkspaceAccess workspaceAccess;
    private final AuditService auditService;

    TicketService(TicketRepository tickets, WorkspaceAccess workspaceAccess, AuditService auditService) {
        this.tickets = tickets;
        this.workspaceAccess = workspaceAccess;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    List<TicketView> list(UUID workspaceId, UUID userId) {
        workspaceAccess.requireMember(workspaceId, userId);
        return tickets.findByWorkspaceIdOrderByUpdatedAtDesc(workspaceId).stream()
                .map(TicketService::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    TicketView get(UUID workspaceId, UUID ticketId, UUID userId) {
        workspaceAccess.requireMember(workspaceId, userId);
        return toView(requireTicket(workspaceId, ticketId));
    }

    @Transactional
    TicketView create(UUID workspaceId, UUID userId, String title, String description) {
        workspaceAccess.requireMember(workspaceId, userId);
        Ticket ticket = tickets.save(new Ticket(
                workspaceId,
                title.trim(),
                normalizeDescription(description),
                userId));
        auditService.record(
                workspaceId,
                userId,
                "TICKET_CREATED",
                "TICKET",
                ticket.id(),
                AuditOutcome.SUCCEEDED);
        return toView(ticket);
    }

    @Transactional
    TicketView changeStatus(
            UUID workspaceId,
            UUID ticketId,
            UUID userId,
            TicketStatus nextStatus) {
        workspaceAccess.requireAnyRole(
                workspaceId, userId, WorkspaceRole.MANAGER, WorkspaceRole.ADMIN);
        Ticket ticket = tickets.lockByWorkspaceIdAndId(workspaceId, ticketId)
                .orElseThrow(() -> ApiException.notFound("Ticket not found"));
        if (!ticket.status().canTransitionTo(nextStatus)) {
            throw ApiException.conflict(
                    "Ticket status must advance OPEN -> IN_PROGRESS -> DONE");
        }
        ticket.transitionTo(nextStatus);
        auditService.record(
                workspaceId,
                userId,
                "TICKET_STATUS_CHANGED",
                "TICKET",
                ticket.id(),
                AuditOutcome.SUCCEEDED);
        return toView(ticket);
    }

    private Ticket requireTicket(UUID workspaceId, UUID ticketId) {
        return tickets.findByWorkspaceIdAndId(workspaceId, ticketId)
                .orElseThrow(() -> ApiException.notFound("Ticket not found"));
    }

    private static String normalizeDescription(String description) {
        return StringUtils.hasText(description) ? description.trim() : null;
    }

    private static TicketView toView(Ticket ticket) {
        return new TicketView(
                ticket.id(),
                ticket.workspaceId(),
                ticket.title(),
                ticket.description(),
                ticket.status(),
                ticket.createdByUserId(),
                ticket.assignedToUserId(),
                ticket.createdAt(),
                ticket.updatedAt());
    }
}
