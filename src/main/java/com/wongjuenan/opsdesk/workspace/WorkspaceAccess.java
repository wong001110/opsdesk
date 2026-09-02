package com.wongjuenan.opsdesk.workspace;

import java.util.Arrays;
import java.util.UUID;

import com.wongjuenan.opsdesk.common.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkspaceAccess {

    private final WorkspaceMembershipRepository memberships;

    public WorkspaceAccess(WorkspaceMembershipRepository memberships) {
        this.memberships = memberships;
    }

    @Transactional(readOnly = true)
    public MembershipAccess requireMember(UUID workspaceId, UUID userId) {
        WorkspaceMembership membership = memberships.findMembership(workspaceId, userId)
                .filter(WorkspaceMembership::active)
                .orElseThrow(() -> ApiException.notFound("Workspace not found"));
        return new MembershipAccess(workspaceId, userId, membership.role());
    }

    @Transactional(readOnly = true)
    public MembershipAccess requireAnyRole(
            UUID workspaceId,
            UUID userId,
            WorkspaceRole... allowedRoles) {
        MembershipAccess membership = requireMember(workspaceId, userId);
        if (Arrays.stream(allowedRoles).noneMatch(membership.role()::equals)) {
            throw ApiException.forbidden("Insufficient workspace role");
        }
        return membership;
    }

    public record MembershipAccess(UUID workspaceId, UUID userId, WorkspaceRole role) {
    }
}
