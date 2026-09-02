package com.wongjuenan.opsdesk.ai;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/tickets/{ticketId}/ai")
public class TicketAiController {

    private final TicketAiService ticketAi;

    public TicketAiController(TicketAiService ticketAi) {
        this.ticketAi = ticketAi;
    }

    @PostMapping("/classify")
    TicketAiService.ClassificationView classify(
            @PathVariable UUID workspaceId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody AiRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return ticketAi.classify(
                workspaceId, ticketId, request.providerProfileId(), principal.userId());
    }

    @PostMapping("/summarize")
    TicketAiService.SummaryView summarize(
            @PathVariable UUID workspaceId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody AiRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return ticketAi.summarize(
                workspaceId, ticketId, request.providerProfileId(), principal.userId());
    }

    public record AiRequest(@NotNull UUID providerProfileId) {
    }
}
