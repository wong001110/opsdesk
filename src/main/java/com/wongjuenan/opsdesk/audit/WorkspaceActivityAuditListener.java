package com.wongjuenan.opsdesk.audit;

import com.wongjuenan.opsdesk.workspace.WorkspaceActivity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
class WorkspaceActivityAuditListener {

    private final AuditService auditService;

    WorkspaceActivityAuditListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onWorkspaceActivity(WorkspaceActivity activity) {
        boolean workspaceTarget = activity.action() == WorkspaceActivity.Action.WORKSPACE_CREATED;
        auditService.record(
                activity.workspaceId(),
                activity.actorUserId(),
                activity.action().name(),
                workspaceTarget ? "WORKSPACE" : "WORKSPACE_MEMBERSHIP",
                workspaceTarget ? activity.workspaceId() : activity.targetUserId(),
                AuditOutcome.SUCCEEDED);
    }
}
