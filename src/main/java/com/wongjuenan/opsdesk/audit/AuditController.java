package com.wongjuenan.opsdesk.audit;

import java.util.List;
import java.util.UUID;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/audit-events")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    List<AuditEventView> list(
            @PathVariable UUID workspaceId,
            @RequestParam(defaultValue = "100") int limit,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return auditService.list(workspaceId, principal.userId(), limit);
    }
}
