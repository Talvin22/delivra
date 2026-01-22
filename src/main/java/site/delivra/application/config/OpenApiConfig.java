package com.post_hub.iam_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "POST_HUB REST API",
                version = "1.0"
        ),
        security = {@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)}
)
@SecurityScheme(
        name = HttpHeaders.AUTHORIZATION,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Value("${swagger.servers.first}")
    private String firstServer;

    @Value("${swagger.servers.second}")
    private String secondServer;

    @Bean
    public GroupedOpenApi publicApi() {
        SpringDocUtils.getConfig().replaceWithClass(LocalDateTime.class, Long.class);
        SpringDocUtils.getConfig().replaceWithClass(LocalDate.class, Long.class);
        SpringDocUtils.getConfig().replaceWithClass(Date.class, Long.class);

        SpringDocUtils.getConfig().addResponseTypeToIgnore(GrantedAuthority.class);

        return GroupedOpenApi.builder()
                .group("iam-service")
                .packagesToScan("com.post_hub.iam_service")
                .addOpenApiCustomizer(serverCustomizer())
                .build();
    }

    @Bean
    public OpenApiCustomizer serverCustomizer() {
        return openApi -> {
            List<Server> servers = new ArrayList<>();
            if (Objects.nonNull(firstServer)) {
                servers.add(new Server().url(firstServer).description("API Server"));
            }
            if (Objects.nonNull(secondServer)) {
                servers.add(new Server().url(secondServer).description("Second API Server"));
            }
            openApi.setServers(servers);
        };
    }
}
