package com.wongjuenan.opsdesk.workspace;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.wongjuenan.opsdesk.common.ApiException;
import com.wongjuenan.opsdesk.identity.IdentityDirectory;
import com.wongjuenan.opsdesk.identity.IdentityDirectory.UserIdentity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class WorkspaceService {

    private final WorkspaceRepository workspaces;
    private final WorkspaceMembershipRepository memberships;
    private final WorkspaceAccess access;
    private final IdentityDirectory identities;
    private final ApplicationEventPublisher events;

    WorkspaceService(
            WorkspaceRepository workspaces,
            WorkspaceMembershipRepository memberships,
            WorkspaceAccess access,
            IdentityDirectory identities,
            ApplicationEventPublisher events) {
        this.workspaces = workspaces;
        this.memberships = memberships;
        this.access = access;
        this.identities = identities;
        this.events = events;
    }

    @Transactional(readOnly = true)
    List<WorkspaceView> list(UUID userId) {
        return memberships.findActiveByUserId(userId).stream()
                .map(membership -> toView(requireWorkspace(membership.workspaceId()), membership.role()))
                .sorted(Comparator.comparing(WorkspaceView::name))
                .toList();
    }

    @Transactional(readOnly = true)
    WorkspaceView get(UUID workspaceId, UUID userId) {
        WorkspaceAccess.MembershipAccess membership = access.requireMember(workspaceId, userId);
        return toView(requireWorkspace(workspaceId), membership.role());
    }

    @Transactional
    WorkspaceView create(String slug, String name, UUID userId) {
        Workspace workspace = new Workspace(normalizeSlug(slug), name.trim(), userId);
        try {
            workspaces.saveAndFlush(workspace);
            memberships.saveAndFlush(new WorkspaceMembership(workspace.id(), userId, WorkspaceRole.ADMIN));
        } catch (DataIntegrityViolationException exception) {
            throw ApiException.conflict("Workspace slug already exists");
        }
        events.publishEvent(new WorkspaceActivity(
                WorkspaceActivity.Action.WORKSPACE_CREATED, workspace.id(), userId, userId));
        return toView(workspace, WorkspaceRole.ADMIN);
    }

    @Transactional(readOnly = true)
    List<MembershipView> listMemberships(UUID workspaceId, UUID actorUserId) {
        access.requireAnyRole(
                workspaceId, actorUserId, WorkspaceRole.MANAGER, WorkspaceRole.ADMIN);
        return memberships.findActiveByWorkspaceId(workspaceId).stream()
                .map(this::toMembershipView)
                .sorted(Comparator.comparing(view -> view.displayName().toLowerCase(Locale.ROOT)))
                .toList();
    }

    @Transactional
    MembershipView addMembership(
            UUID workspaceId,
            UUID actorUserId,
            String email,
            WorkspaceRole role) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        UserIdentity user = identities.requireByEmail(email);
        if (!user.enabled()) {
            throw ApiException.badRequest("Disabled users cannot join a workspace");
        }

        WorkspaceActivity.Action action;
        WorkspaceMembership membership = memberships.findMembership(workspaceId, user.id()).orElse(null);
        if (membership == null) {
            membership = new WorkspaceMembership(workspaceId, user.id(), role);
            action = WorkspaceActivity.Action.MEMBERSHIP_ADDED;
        } else if (membership.active()) {
            throw ApiException.conflict("User is already an active member");
        } else {
            membership.reactivate(role);
            action = WorkspaceActivity.Action.MEMBERSHIP_REACTIVATED;
        }

        memberships.saveAndFlush(membership);
        events.publishEvent(new WorkspaceActivity(action, workspaceId, actorUserId, user.id()));
        return toMembershipView(membership, user);
    }

    @Transactional
    MembershipView changeRole(
            UUID workspaceId,
            UUID targetUserId,
            UUID actorUserId,
            WorkspaceRole newRole) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        List<WorkspaceMembership> activeMemberships = memberships.lockActiveByWorkspaceId(workspaceId);
        WorkspaceMembership target = activeMemberships.stream()
                .filter(candidate -> candidate.userId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Membership not found"));

        ensureAdminRemains(activeMemberships, target, newRole);
        if (target.role() != newRole) {
            target.changeRole(newRole);
            events.publishEvent(new WorkspaceActivity(
                    WorkspaceActivity.Action.MEMBERSHIP_ROLE_CHANGED,
                    workspaceId,
                    actorUserId,
                    targetUserId));
        }
        return toMembershipView(target);
    }

    @Transactional
    void deactivateMembership(UUID workspaceId, UUID targetUserId, UUID actorUserId) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        List<WorkspaceMembership> activeMemberships = memberships.lockActiveByWorkspaceId(workspaceId);
        WorkspaceMembership target = activeMemberships.stream()
                .filter(candidate -> candidate.userId().equals(targetUserId))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Membership not found"));

        ensureAdminRemains(activeMemberships, target, null);
        target.deactivate();
        events.publishEvent(new WorkspaceActivity(
                WorkspaceActivity.Action.MEMBERSHIP_DEACTIVATED,
                workspaceId,
                actorUserId,
                targetUserId));
    }

    private void ensureAdminRemains(
            List<WorkspaceMembership> activeMemberships,
            WorkspaceMembership target,
            WorkspaceRole replacementRole) {
        if (target.role() != WorkspaceRole.ADMIN || replacementRole == WorkspaceRole.ADMIN) {
            return;
        }
        long adminCount = activeMemberships.stream()
                .filter(member -> member.role() == WorkspaceRole.ADMIN)
                .count();
        if (adminCount <= 1) {
            throw ApiException.conflict("A workspace must retain at least one active ADMIN");
        }
    }

    private Workspace requireWorkspace(UUID workspaceId) {
        return workspaces.findById(workspaceId)
                .orElseThrow(() -> ApiException.notFound("Workspace not found"));
    }

    private MembershipView toMembershipView(WorkspaceMembership membership) {
        return toMembershipView(membership, identities.requireById(membership.userId()));
    }

    private MembershipView toMembershipView(WorkspaceMembership membership, UserIdentity user) {
        return new MembershipView(
                membership.userId(),
                user.email(),
                user.displayName(),
                membership.role(),
                membership.active());
    }

    private static WorkspaceView toView(Workspace workspace, WorkspaceRole role) {
        return new WorkspaceView(workspace.id(), workspace.slug(), workspace.name(), role);
    }

    private static String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    record WorkspaceView(UUID id, String slug, String name, WorkspaceRole role) {
    }

    record MembershipView(
            UUID userId,
            String email,
            String displayName,
            WorkspaceRole role,
            boolean active) {
    }
}
