package com.wongjuenan.opsdesk.ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByWorkspaceIdOrderByUpdatedAtDesc(UUID workspaceId);

    Optional<Ticket> findByWorkspaceIdAndId(UUID workspaceId, UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.workspaceId = :workspaceId and t.id = :ticketId")
    Optional<Ticket> lockByWorkspaceIdAndId(
            @Param("workspaceId") UUID workspaceId,
            @Param("ticketId") UUID ticketId);
}
