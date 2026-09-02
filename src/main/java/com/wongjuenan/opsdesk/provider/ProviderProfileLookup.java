package com.wongjuenan.opsdesk.provider;

import java.util.UUID;

public interface ProviderProfileLookup {

    ProviderForUse requireProfileForReadOnlyAnalysis(UUID workspaceId, UUID profileId);

    record ProviderForUse(
            UUID id,
            ProviderType providerType,
            String trustedOrigin,
            String model,
            String credentialReference) {
    }
}
