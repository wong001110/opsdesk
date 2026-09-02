package com.wongjuenan.opsdesk.provider;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Deployment-owned AI configuration. It intentionally contains no credential value.
 */
@ConfigurationProperties(prefix = "opsdesk.ai")
public class AiProviderProperties {

    private boolean liveEnabled;
    private DeepSeek deepseek = new DeepSeek();

    public boolean isLiveEnabled() {
        return liveEnabled;
    }

    public void setLiveEnabled(boolean liveEnabled) {
        this.liveEnabled = liveEnabled;
    }

    public DeepSeek getDeepseek() {
        return deepseek;
    }

    public void setDeepseek(DeepSeek deepseek) {
        this.deepseek = deepseek;
    }

    public static class DeepSeek {

        private String baseUrl = "https://api.deepseek.com";
        private String model = "deepseek-chat";
        private String credentialEnvironment = "OPSDESK_DEEPSEEK_API_KEY";
        private Duration timeout = Duration.ofSeconds(10);
        private int maxTokens = 160;
        private int maxInputChars = 8_000;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getCredentialEnvironment() { return credentialEnvironment; }
        public void setCredentialEnvironment(String credentialEnvironment) { this.credentialEnvironment = credentialEnvironment; }
        public Duration getTimeout() { return timeout; }
        public void setTimeout(Duration timeout) { this.timeout = timeout; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
        public int getMaxInputChars() { return maxInputChars; }
        public void setMaxInputChars(int maxInputChars) { this.maxInputChars = maxInputChars; }
    }
}
