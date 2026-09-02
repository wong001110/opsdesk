CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    email_normalized VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_app_user_email_normalized
        CHECK (email_normalized = LOWER(TRIM(email))),
    CONSTRAINT ck_app_user_version
        CHECK (version >= 0)
);

CREATE TABLE workspace (
    id UUID PRIMARY KEY,
    slug VARCHAR(80) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_workspace_created_by_user
        FOREIGN KEY (created_by_user_id) REFERENCES app_user (id),
    CONSTRAINT ck_workspace_version
        CHECK (version >= 0)
);

CREATE TABLE workspace_membership (
    workspace_id UUID NOT NULL,
    user_id UUID NOT NULL,
    membership_role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (workspace_id, user_id),
    CONSTRAINT fk_membership_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT fk_membership_user
        FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT ck_membership_role
        CHECK (membership_role IN ('ADMIN', 'MANAGER', 'MEMBER')),
    CONSTRAINT ck_membership_version
        CHECK (version >= 0)
);

CREATE TABLE ticket (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(8000),
    ticket_status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_by_user_id UUID NOT NULL,
    assigned_to_user_id UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_ticket_workspace_id UNIQUE (workspace_id, id),
    CONSTRAINT fk_ticket_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT fk_ticket_created_by_membership
        FOREIGN KEY (workspace_id, created_by_user_id)
        REFERENCES workspace_membership (workspace_id, user_id),
    CONSTRAINT fk_ticket_assigned_to_membership
        FOREIGN KEY (workspace_id, assigned_to_user_id)
        REFERENCES workspace_membership (workspace_id, user_id),
    CONSTRAINT ck_ticket_status
        CHECK (ticket_status IN ('OPEN', 'IN_PROGRESS', 'DONE')),
    CONSTRAINT ck_ticket_version
        CHECK (version >= 0)
);

CREATE TABLE provider_profile (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    profile_name VARCHAR(120) NOT NULL,
    provider_type VARCHAR(80) NOT NULL,
    base_url VARCHAR(2048) NOT NULL,
    credential_reference VARCHAR(500) NOT NULL,
    created_by_user_id UUID NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_provider_profile_workspace_id UNIQUE (workspace_id, id),
    CONSTRAINT uq_provider_profile_workspace_name
        UNIQUE (workspace_id, profile_name),
    CONSTRAINT fk_provider_profile_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT fk_provider_profile_created_by_membership
        FOREIGN KEY (workspace_id, created_by_user_id)
        REFERENCES workspace_membership (workspace_id, user_id),
    CONSTRAINT ck_provider_profile_version
        CHECK (version >= 0)
);

CREATE TABLE audit_event (
    id UUID PRIMARY KEY,
    workspace_id UUID NOT NULL,
    actor_user_id UUID,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id UUID,
    outcome VARCHAR(20) NOT NULL,
    correlation_id UUID,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_audit_event_workspace_id UNIQUE (workspace_id, id),
    CONSTRAINT fk_audit_event_workspace
        FOREIGN KEY (workspace_id) REFERENCES workspace (id),
    CONSTRAINT fk_audit_event_actor_user
        FOREIGN KEY (actor_user_id) REFERENCES app_user (id),
    CONSTRAINT ck_audit_event_outcome
        CHECK (outcome IN ('SUCCEEDED', 'DENIED', 'FAILED'))
);

CREATE INDEX ix_membership_user_active
    ON workspace_membership (user_id, active);

CREATE INDEX ix_ticket_workspace_status_updated
    ON ticket (workspace_id, ticket_status, updated_at);

CREATE INDEX ix_ticket_workspace_assignee
    ON ticket (workspace_id, assigned_to_user_id);

CREATE INDEX ix_provider_profile_workspace_enabled
    ON provider_profile (workspace_id, enabled);

CREATE INDEX ix_audit_event_workspace_occurred
    ON audit_event (workspace_id, occurred_at);

CREATE INDEX ix_audit_event_correlation
    ON audit_event (correlation_id);
