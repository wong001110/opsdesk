package com.wongjuenan.opsdesk.provider;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProviderProfileRepository extends JpaRepository<ProviderProfile, UUID> {

    List<ProviderProfile> findByWorkspaceIdAndEnabledTrueOrderByProfileNameAsc(UUID workspaceId);

    Optional<ProviderProfile> findByWorkspaceIdAndIdAndEnabledTrue(UUID workspaceId, UUID id);
}
