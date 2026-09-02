package com.wongjuenan.opsdesk.ai;

import java.util.UUID;

import com.wongjuenan.opsdesk.ai.AiTicketReader.TicketText;
import com.wongjuenan.opsdesk.audit.AuditOutcome;
import com.wongjuenan.opsdesk.audit.AuditService;
import com.wongjuenan.opsdesk.common.ApiException;
import com.wongjuenan.opsdesk.provider.ProviderProfileLookup;
import com.wongjuenan.opsdesk.provider.ProviderProfileLookup.ProviderForUse;
import com.wongjuenan.opsdesk.provider.ProviderType;
import com.wongjuenan.opsdesk.workspace.WorkspaceAccess;
import com.wongjuenan.opsdesk.workspace.WorkspaceRole;
import org.springframework.stereotype.Service;

@Service
class TicketAiService {

    private final WorkspaceAccess access;
    private final AiTicketReader tickets;
    private final ProviderProfileLookup providers;
    private final AuditService audit;
    private final AiProviderRouter providerRouter;

    TicketAiService(
            WorkspaceAccess access,
            AiTicketReader tickets,
            ProviderProfileLookup providers,
            AuditService audit,
            AiProviderRouter providerRouter) {
        this.access = access;
        this.tickets = tickets;
        this.providers = providers;
        this.audit = audit;
        this.providerRouter = providerRouter;
    }

    ClassificationView classify(
            UUID workspaceId,
            UUID ticketId,
            UUID profileId,
            UUID actorUserId) {
        access.requireMember(workspaceId, actorUserId);
        TicketText ticket = requireTicket(workspaceId, ticketId);
        ProviderForUse provider = providers.requireProfileForReadOnlyAnalysis(workspaceId, profileId);
        requireLiveProviderRole(workspaceId, actorUserId, provider);
        String classification = executeClassification(workspaceId, actorUserId, ticket, provider);
        audit.record(
                workspaceId,
                actorUserId,
                "AI_TICKET_CLASSIFIED",
                "TICKET",
                ticket.id(),
                AuditOutcome.SUCCEEDED);
        return new ClassificationView(ticket.id(), provider.id(), provider.providerType().name(), classification);
    }

    SummaryView summarize(
            UUID workspaceId,
            UUID ticketId,
            UUID profileId,
            UUID actorUserId) {
        access.requireMember(workspaceId, actorUserId);
        TicketText ticket = requireTicket(workspaceId, ticketId);
        ProviderForUse provider = providers.requireProfileForReadOnlyAnalysis(workspaceId, profileId);
        requireLiveProviderRole(workspaceId, actorUserId, provider);
        String summary = executeSummary(workspaceId, actorUserId, ticket, provider);
        audit.record(
                workspaceId,
                actorUserId,
                "AI_TICKET_SUMMARIZED",
                "TICKET",
                ticket.id(),
                AuditOutcome.SUCCEEDED);
        return new SummaryView(ticket.id(), provider.id(), provider.providerType().name(), summary);
    }

    private TicketText requireTicket(UUID workspaceId, UUID ticketId) {
        return tickets.findByWorkspaceIdAndId(workspaceId, ticketId)
                .orElseThrow(() -> ApiException.notFound("Ticket not found"));
    }

    private String executeClassification(UUID workspaceId, UUID actorUserId, TicketText ticket, ProviderForUse provider) {
        try {
            return providerRouter.classify(provider, ticket);
        } catch (ApiException exception) {
            audit.record(workspaceId, actorUserId, "AI_TICKET_CLASSIFICATION_FAILED", "TICKET", ticket.id(), AuditOutcome.FAILED);
            throw exception;
        }
    }

    private String executeSummary(UUID workspaceId, UUID actorUserId, TicketText ticket, ProviderForUse provider) {
        try {
            return providerRouter.summarize(provider, ticket);
        } catch (ApiException exception) {
            audit.record(workspaceId, actorUserId, "AI_TICKET_SUMMARY_FAILED", "TICKET", ticket.id(), AuditOutcome.FAILED);
            throw exception;
        }
    }

    private void requireLiveProviderRole(UUID workspaceId, UUID actorUserId, ProviderForUse provider) {
        if (provider.providerType() == ProviderType.DEEPSEEK) {
            access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.MANAGER, WorkspaceRole.ADMIN);
        }
    }

    record ClassificationView(
            UUID ticketId,
            UUID providerProfileId,
            String mode,
            String classification) {
    }

    record SummaryView(
            UUID ticketId,
            UUID providerProfileId,
            String mode,
            String summary) {
    }
}
