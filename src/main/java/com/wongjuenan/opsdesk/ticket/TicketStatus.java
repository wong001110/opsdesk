package com.wongjuenan.opsdesk.ticket;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    DONE;

    boolean canTransitionTo(TicketStatus next) {
        return (this == OPEN && next == IN_PROGRESS)
                || (this == IN_PROGRESS && next == DONE);
    }
}
