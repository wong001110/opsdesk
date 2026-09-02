package com.wongjuenan.opsdesk.provider;

import java.net.URI;

import com.wongjuenan.opsdesk.common.ApiException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * A single allowlist boundary for outbound model execution. Future sensitive
 * capabilities must be added here explicitly; profiles never grant them by default.
 */
@Component
public class ProviderExecutionPolicy {

    public enum Capability { READ_ONLY_ANALYSIS }

    private static final String MOCK_ORIGIN = "https://mock.invalid";
    private final AiProviderProperties properties;

    public ProviderExecutionPolicy(AiProviderProperties properties) {
        this.properties = properties;
    }

    public String deepSeekOrigin() {
        return normalizeOrigin(properties.getDeepseek().getBaseUrl());
    }

    public String deepSeekModel() {
        String model = properties.getDeepseek().getModel();
        if (!StringUtils.hasText(model) || model.length() > 200) {
            throw ApiException.badRequest("Configured model is invalid");
        }
        return model.trim();
    }

    public void validateRuntimeLimits() {
        if (properties.getDeepseek().getTimeout() == null
                || properties.getDeepseek().getTimeout().isNegative()
                || properties.getDeepseek().getTimeout().isZero()
                || properties.getDeepseek().getTimeout().compareTo(java.time.Duration.ofSeconds(30)) > 0
                || properties.getDeepseek().getMaxTokens() < 1
                || properties.getDeepseek().getMaxTokens() > 300
                || properties.getDeepseek().getMaxInputChars() < 1
                || properties.getDeepseek().getMaxInputChars() > 8_000) {
            throw ApiException.badRequest("Configured model limits are invalid");
        }
    }

    public String deepSeekCredentialReference() {
        String environment = properties.getDeepseek().getCredentialEnvironment();
        if (environment == null || !environment.matches("[A-Za-z_][A-Za-z0-9_]{0,127}")) {
            throw ApiException.badRequest("Configured credential environment is invalid");
        }
        return "env:" + environment;
    }

    public boolean deepSeekImportAvailable() {
        if (!properties.isLiveEnabled()) {
            return false;
        }
        try {
            validateRuntimeLimits();
            return StringUtils.hasText(System.getenv(properties.getDeepseek().getCredentialEnvironment()))
                    && StringUtils.hasText(deepSeekOrigin())
                    && StringUtils.hasText(deepSeekModel());
        } catch (ApiException exception) {
            return false;
        }
    }

    public void requireCapability(ProviderProfileLookup.ProviderForUse provider, Capability capability) {
        if (capability != Capability.READ_ONLY_ANALYSIS) {
            throw ApiException.forbidden("Provider capability is not enabled");
        }
        if (provider.providerType() == ProviderType.MOCK
                && MOCK_ORIGIN.equals(provider.trustedOrigin())) {
            return;
        }
        if (provider.providerType() == ProviderType.DEEPSEEK
                && properties.isLiveEnabled()
                && deepSeekImportAvailable()
                && deepSeekOrigin().equals(provider.trustedOrigin())
                && deepSeekModel().equals(provider.model())
                && deepSeekCredentialReference().equals(provider.credentialReference())) {
            validateRuntimeLimits();
            return;
        }
        throw ApiException.forbidden("Provider is not enabled for this capability");
    }

    static String normalizeOrigin(String rawOrigin) {
        try {
            URI parsed = new URI(rawOrigin == null ? "" : rawOrigin.trim());
            String path = parsed.getPath();
            if (!"https".equalsIgnoreCase(parsed.getScheme())
                    || parsed.getHost() == null
                    || parsed.getUserInfo() != null
                    || (path != null && !path.isEmpty() && !"/".equals(path))
                    || parsed.getQuery() != null
                    || parsed.getFragment() != null) {
                throw ApiException.badRequest("Configured provider origin is invalid");
            }
            int port = parsed.getPort() == 443 ? -1 : parsed.getPort();
            return new URI("https", null, parsed.getHost().toLowerCase(), port, null, null, null).toASCIIString();
        } catch (Exception exception) {
            if (exception instanceof ApiException apiException) {
                throw apiException;
            }
            throw ApiException.badRequest("Configured provider origin is invalid");
        }
    }
}
