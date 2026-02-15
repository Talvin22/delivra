package site.delivra.application.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@Getter
public class HereApiConfig {

    @Value("${here.api.key}")
    private String apiKey;

    @Value("${here.api.geocoding.base-url}")
    private String geocodingBaseUrl;

    @Value("${here.api.routing.base-url}")
    private String routingBaseUrl;

    @Value("${here.api.truck.default-gross-weight}")
    private Integer defaultGrossWeight;

    @Value("${here.api.truck.default-height}")
    private Integer defaultHeight;

    @Value("${here.api.truck.default-width}")
    private Integer defaultWidth;

    @Value("${here.api.truck.default-length}")
    private Integer defaultLength;

    @Bean
    public RestTemplate hereRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }
}
