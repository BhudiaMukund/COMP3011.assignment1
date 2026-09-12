package au.edu.adelaide.stt.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Automatically maps all the openai.* settings from our application.yml file into this Java object.
 *
 * Security Rule:
 * We never hardcode the API key in our code or config files because anyone looking at 
 * the repository could steal it. Instead, it gets loaded securely from the 
 * environment variables (OPENAI_API_KEY) when the app starts.
 *
 * Preventing Accidental Leaks:
 * By default, Java records will automatically print all of their variables if you try 
 * to log them. We deliberately rewrite the toString() method here so that if a developer 
 * accidentally prints this settings object to the console, it safely hides the secret API 
 * key and only shows safe information!
 */
@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        String baseUrl,
        String transcriptionsPath,
        String model,
        String apiKey,
        Duration connectTimeout,
        Duration readTimeout) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String toString() {
        return "OpenAiProperties[baseUrl=%s, model=%s, apiKeyPresent=%s]"
                .formatted(baseUrl, model, hasApiKey());
    }
}