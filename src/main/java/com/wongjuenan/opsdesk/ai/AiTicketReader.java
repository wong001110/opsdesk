package com.wongjuenan.opsdesk.ai;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class AiTicketReader {

    private final JdbcTemplate jdbc;

    AiTicketReader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    Optional<TicketText> findByWorkspaceIdAndId(UUID workspaceId, UUID ticketId) {
        List<TicketText> rows = jdbc.query(
                "select id, title, description from ticket where workspace_id = ? and id = ?",
                (resultSet, rowNumber) -> new TicketText(
                        resultSet.getObject("id", UUID.class),
                        resultSet.getString("title"),
                        resultSet.getString("description")),
                workspaceId,
                ticketId);
        return rows.stream().findFirst();
    }

    record TicketText(UUID id, String title, String description) {
    }
}
