package com.wongjuenan.opsdesk.provider;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.wongjuenan.opsdesk.audit.AuditOutcome;
import com.wongjuenan.opsdesk.audit.AuditService;
import com.wongjuenan.opsdesk.common.ApiException;
import com.wongjuenan.opsdesk.workspace.WorkspaceAccess;
import com.wongjuenan.opsdesk.workspace.WorkspaceRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ProviderProfileService implements ProviderProfileLookup {

    private static final Pattern ENV_REFERENCE = Pattern.compile("env:[A-Za-z_][A-Za-z0-9_]{0,127}");
    private static final Pattern SECRET_REFERENCE =
            Pattern.compile("secret://[A-Za-z0-9][A-Za-z0-9._/-]{0,470}");
    private static final String MOCK_ORIGIN = "https://mock.invalid";
    private static final String MOCK_MODEL = "mock-local";
    private static final String DEEPSEEK_IMPORT_ID = "deepseek-server-config";

    private final ProviderProfileRepository profiles;
    private final WorkspaceAccess access;
    private final AuditService audit;
    private final ProviderExecutionPolicy executionPolicy;

    ProviderProfileService(
            ProviderProfileRepository profiles,
            WorkspaceAccess access,
            AuditService audit,
            ProviderExecutionPolicy executionPolicy) {
        this.profiles = profiles;
        this.access = access;
        this.audit = audit;
        this.executionPolicy = executionPolicy;
    }

    @Transactional(readOnly = true)
    List<ProviderProfileView> list(UUID workspaceId, UUID actorUserId) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.MANAGER, WorkspaceRole.ADMIN);
        return profiles.findByWorkspaceIdAndEnabledTrueOrderByProfileNameAsc(workspaceId).stream()
                .map(ProviderProfileService::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    ProviderProfileView get(UUID workspaceId, UUID profileId, UUID actorUserId) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.MANAGER, WorkspaceRole.ADMIN);
        return toView(requireScopedProfile(workspaceId, profileId));
    }

    @Transactional
    ProviderProfileView create(
            UUID workspaceId,
            UUID actorUserId,
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String credentialReference) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        ProviderConfiguration configuration = normalize(
                profileName, providerType, trustedOrigin, credentialReference);
        ProviderProfile profile = new ProviderProfile(
                workspaceId,
                configuration.profileName(),
                configuration.providerType(),
                configuration.trustedOrigin(),
                configuration.model(),
                configuration.credentialReference(),
                actorUserId);
        try {
            profiles.saveAndFlush(profile);
        } catch (DataIntegrityViolationException exception) {
            throw ApiException.conflict("Provider profile name already exists");
        }
        audit.record(
                workspaceId,
                actorUserId,
                "PROVIDER_PROFILE_CREATED",
                "PROVIDER_PROFILE",
                profile.id(),
                AuditOutcome.SUCCEEDED);
        return toView(profile);
    }

    @Transactional
    ProviderProfileView update(
            UUID workspaceId,
            UUID profileId,
            UUID actorUserId,
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String credentialReference) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        ProviderConfiguration configuration = normalize(
                profileName, providerType, trustedOrigin, credentialReference);
        ProviderProfile profile = requireScopedProfile(workspaceId, profileId);
        profile.reconfigure(
                configuration.profileName(),
                configuration.providerType(),
                configuration.trustedOrigin(),
                configuration.model(),
                configuration.credentialReference());
        try {
            profiles.saveAndFlush(profile);
        } catch (DataIntegrityViolationException exception) {
            throw ApiException.conflict("Provider profile name already exists");
        }
        audit.record(
                workspaceId,
                actorUserId,
                "PROVIDER_PROFILE_UPDATED",
                "PROVIDER_PROFILE",
                profile.id(),
                AuditOutcome.SUCCEEDED);
        return toView(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderForUse requireProfileForReadOnlyAnalysis(UUID workspaceId, UUID profileId) {
        ProviderProfile profile = requireScopedProfile(workspaceId, profileId);
        return new ProviderForUse(
                profile.id(),
                profile.providerType(),
                profile.trustedOrigin(),
                profile.model(),
                profile.credentialReference());
    }

    @Transactional(readOnly = true)
    List<ProviderImportOptionView> importOptions(UUID workspaceId, UUID actorUserId) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        if (!executionPolicy.deepSeekImportAvailable()) {
            return List.of();
        }
        return List.of(new ProviderImportOptionView(
                DEEPSEEK_IMPORT_ID,
                "DeepSeek server configuration",
                ProviderType.DEEPSEEK,
                executionPolicy.deepSeekModel(),
                executionPolicy.deepSeekOrigin()));
    }

    @Transactional
    ProviderProfileView importProfile(
            UUID workspaceId,
            UUID actorUserId,
            String profileName,
            String importOptionId) {
        access.requireAnyRole(workspaceId, actorUserId, WorkspaceRole.ADMIN);
        if (!DEEPSEEK_IMPORT_ID.equals(importOptionId) || !executionPolicy.deepSeekImportAvailable()) {
            throw ApiException.badRequest("Selected provider import is not available");
        }
        return createConfiguredProfile(
                workspaceId,
                actorUserId,
                profileName,
                ProviderType.DEEPSEEK,
                executionPolicy.deepSeekOrigin(),
                executionPolicy.deepSeekModel(),
                executionPolicy.deepSeekCredentialReference(),
                "PROVIDER_PROFILE_IMPORTED");
    }

    private ProviderProfile requireScopedProfile(UUID workspaceId, UUID profileId) {
        return profiles.findByWorkspaceIdAndIdAndEnabledTrue(workspaceId, profileId)
                .orElseThrow(() -> ApiException.notFound("Provider profile not found"));
    }

    private static ProviderConfiguration normalize(
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String credentialReference) {
        String normalizedName = profileName.trim();
        String normalizedOrigin = ProviderExecutionPolicy.normalizeOrigin(trustedOrigin);
        String normalizedReference = credentialReference.trim();
        if (!ENV_REFERENCE.matcher(normalizedReference).matches()
                && !SECRET_REFERENCE.matcher(normalizedReference).matches()) {
            throw ApiException.badRequest(
                    "Credential reference must use env:NAME or secret://... syntax");
        }
        if (providerType == ProviderType.MOCK && !MOCK_ORIGIN.equals(normalizedOrigin)) {
            throw ApiException.badRequest("MOCK provider origin must be https://mock.invalid");
        }
        if (providerType != ProviderType.MOCK) {
            throw ApiException.badRequest("Live providers must be imported from server model configuration");
        }
        return new ProviderConfiguration(
                normalizedName, providerType, normalizedOrigin, MOCK_MODEL, normalizedReference);
    }

    private static ProviderProfileView toView(ProviderProfile profile) {
        return new ProviderProfileView(
                profile.id(),
                profile.profileName(),
                profile.providerType(),
                profile.trustedOrigin(),
                profile.model(),
                profile.credentialConfigured(),
                profile.enabled());
    }

    private ProviderProfileView createConfiguredProfile(
            UUID workspaceId,
            UUID actorUserId,
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String model,
            String credentialReference,
            String auditAction) {
        String normalizedName = profileName.trim();
        ProviderProfile profile = new ProviderProfile(
                workspaceId,
                normalizedName,
                providerType,
                trustedOrigin,
                model,
                credentialReference,
                actorUserId);
        try {
            profiles.saveAndFlush(profile);
        } catch (DataIntegrityViolationException exception) {
            throw ApiException.conflict("Provider profile name already exists");
        }
        audit.record(workspaceId, actorUserId, auditAction, "PROVIDER_PROFILE", profile.id(), AuditOutcome.SUCCEEDED);
        return toView(profile);
    }

    private record ProviderConfiguration(
            String profileName,
            ProviderType providerType,
            String trustedOrigin,
            String model,
            String credentialReference) {
    }

    record ProviderProfileView(
            UUID id,
            String name,
            ProviderType providerType,
            String trustedOrigin,
            String model,
            boolean credentialConfigured,
            boolean enabled) {
    }

    record ProviderImportOptionView(
            String id,
            String label,
            ProviderType providerType,
            String model,
            String trustedOrigin) {
    }
}
