package com.wongjuenan.opsdesk.provider;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.wongjuenan.opsdesk.security.OpsDeskPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/provider-profiles")
public class ProviderProfileController {

    private final ProviderProfileService providerProfiles;

    public ProviderProfileController(ProviderProfileService providerProfiles) {
        this.providerProfiles = providerProfiles;
    }

    @GetMapping
    List<ProviderProfileService.ProviderProfileView> list(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return providerProfiles.list(workspaceId, principal.userId());
    }

    @GetMapping("/import-options")
    ProviderImportOptionsResponse importOptions(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return new ProviderImportOptionsResponse(providerProfiles.importOptions(workspaceId, principal.userId()));
    }

    @PostMapping("/import")
    ResponseEntity<ProviderProfileService.ProviderProfileView> importProfile(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody ImportProviderProfileRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        ProviderProfileService.ProviderProfileView profile = providerProfiles.importProfile(
                workspaceId, principal.userId(), request.name(), request.importOptionId());
        URI location = URI.create("/api/v1/workspaces/" + workspaceId
                + "/provider-profiles/" + profile.id());
        return ResponseEntity.created(location).body(profile);
    }

    @PostMapping
    ResponseEntity<ProviderProfileService.ProviderProfileView> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody SaveProviderProfileRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        ProviderProfileService.ProviderProfileView profile = providerProfiles.create(
                workspaceId,
                principal.userId(),
                request.name(),
                request.providerType(),
                request.trustedOrigin(),
                request.credentialReference());
        URI location = URI.create("/api/v1/workspaces/" + workspaceId
                + "/provider-profiles/" + profile.id());
        return ResponseEntity.created(location).body(profile);
    }

    @GetMapping("/{profileId}")
    ProviderProfileService.ProviderProfileView get(
            @PathVariable UUID workspaceId,
            @PathVariable UUID profileId,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return providerProfiles.get(workspaceId, profileId, principal.userId());
    }

    @PutMapping("/{profileId}")
    ProviderProfileService.ProviderProfileView update(
            @PathVariable UUID workspaceId,
            @PathVariable UUID profileId,
            @Valid @RequestBody SaveProviderProfileRequest request,
            @AuthenticationPrincipal OpsDeskPrincipal principal) {
        return providerProfiles.update(
                workspaceId,
                profileId,
                principal.userId(),
                request.name(),
                request.providerType(),
                request.trustedOrigin(),
                request.credentialReference());
    }

    public record SaveProviderProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @NotNull ProviderType providerType,
            @NotBlank @Size(max = 2048) String trustedOrigin,
            @NotBlank @Size(max = 500) String credentialReference) {
    }

    public record ImportProviderProfileRequest(
            @NotBlank @Size(max = 120) String name,
            @NotBlank @Size(max = 80) String importOptionId) {
    }

    public record ProviderImportOptionsResponse(List<ProviderProfileService.ProviderImportOptionView> options) {
    }
}
