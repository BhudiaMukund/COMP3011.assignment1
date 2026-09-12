package au.edu.adelaide.stt.config;

import java.net.http.HttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Sets up  web client we use to talk to external OpenAI speech-to-text server.
 *
 * We use Java's newer HttpClient because it works perfectly with virtual threads.
 * If the OpenAI server crashes or takes forever to respond, our app shouldn't 
 * freeze waiting for it. By setting strict timeouts, we ensure that 
 * if the external service is hanging, our app will safely cut the connection and return 
 * an error instead of waiting forever.
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestClient openAiRestClient(OpenAiProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .build();
    }
}