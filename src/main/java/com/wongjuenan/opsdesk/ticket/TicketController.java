package com.wongjuenan.opsdesk.ticket;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    List<TicketView> list(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return ticketService.list(workspaceId, principal.userId());
    }

    @PostMapping
    ResponseEntity<TicketView> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        TicketView ticket = ticketService.create(
                workspaceId, principal.userId(), request.title(), request.description());
        URI location = URI.create("/api/v1/workspaces/" + workspaceId + "/tickets/" + ticket.id());
        return ResponseEntity.created(location).body(ticket);
    }

    @GetMapping("/{ticketId}")
    TicketView get(
            @PathVariable UUID workspaceId,
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return ticketService.get(workspaceId, ticketId, principal.userId());
    }

    @PatchMapping("/{ticketId}/status")
    TicketView changeStatus(
            @PathVariable UUID workspaceId,
            @PathVariable UUID ticketId,
            @Valid @RequestBody ChangeTicketStatusRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return ticketService.changeStatus(workspaceId, ticketId, principal.userId(), request.status());
    }

    public record CreateTicketRequest(
            @NotBlank @Size(max = 200) String title,
            @Size(max = 8000) String description) {
    }

    public record ChangeTicketStatusRequest(@NotNull TicketStatus status) {
    }
}
