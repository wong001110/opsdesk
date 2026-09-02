package com.wongjuenan.opsdesk.workspace;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface WorkspaceMembershipRepository
        extends JpaRepository<WorkspaceMembership, WorkspaceMembershipId> {

    @Query("select m from WorkspaceMembership m where m.id.userId = :userId and m.active = true")
    List<WorkspaceMembership> findActiveByUserId(@Param("userId") UUID userId);

    @Query("select m from WorkspaceMembership m where m.id.workspaceId = :workspaceId and m.active = true")
    List<WorkspaceMembership> findActiveByWorkspaceId(@Param("workspaceId") UUID workspaceId);

    @Query("select m from WorkspaceMembership m where m.id.workspaceId = :workspaceId "
            + "and m.id.userId = :userId")
    Optional<WorkspaceMembership> findMembership(
            @Param("workspaceId") UUID workspaceId,
            @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from WorkspaceMembership m where m.id.workspaceId = :workspaceId and m.active = true")
    List<WorkspaceMembership> lockActiveByWorkspaceId(@Param("workspaceId") UUID workspaceId);
}
