package com.wongjuenan.opsdesk.workspace;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    public WorkspaceController(WorkspaceService workspaceService) {
        this.workspaceService = workspaceService;
    }

    @GetMapping
    List<WorkspaceService.WorkspaceView> list(@AuthenticationPrincipal OpsDeskPrincipal principal) {
        return workspaceService.list(principal.userId());
    }

    @PostMapping
    ResponseEntity<WorkspaceService.WorkspaceView> create(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        WorkspaceService.WorkspaceView workspace = workspaceService.create(
                request.slug(), request.name(), principal.userId());
        return ResponseEntity.created(URI.create("/api/v1/workspaces/" + workspace.id())).body(workspace);
    }

    @GetMapping("/{workspaceId}")
    WorkspaceService.WorkspaceView get(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return workspaceService.get(workspaceId, principal.userId());
    }

    @GetMapping("/{workspaceId}/memberships")
    List<WorkspaceService.MembershipView> listMemberships(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return workspaceService.listMemberships(workspaceId, principal.userId());
    }

    @PostMapping("/{workspaceId}/memberships")
    ResponseEntity<WorkspaceService.MembershipView> addMembership(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody AddMembershipRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        WorkspaceService.MembershipView membership = workspaceService.addMembership(
                workspaceId, principal.userId(), request.email(), request.role());
        URI location = URI.create("/api/v1/workspaces/" + workspaceId
                + "/memberships/" + membership.userId());
        return ResponseEntity.created(location).body(membership);
    }

    @PatchMapping("/{workspaceId}/memberships/{userId}")
    WorkspaceService.MembershipView changeRole(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeRoleRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return workspaceService.changeRole(workspaceId, userId, principal.userId(), request.role());
    }

    @DeleteMapping("/{workspaceId}/memberships/{userId}")
    ResponseEntity<Void> deactivateMembership(
            @PathVariable UUID workspaceId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        workspaceService.deactivateMembership(workspaceId, userId, principal.userId());
        return ResponseEntity.noContent().build();
    }

    public record CreateWorkspaceRequest(
            @NotBlank @Size(max = 80) String slug,
            @NotBlank @Size(max = 160) String name) {
    }

    public record AddMembershipRequest(
            @NotBlank @Email @Size(max = 320) String email,
            @NotNull WorkspaceRole role) {
    }

    public record ChangeRoleRequest(@NotNull WorkspaceRole role) {
    }
}
