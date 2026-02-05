package com.wit.be.common.config.swagger;

import com.wit.be.infra.properties.UrlProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String ACCESS_TOKEN = "JWT";
    private static final String AUTHORIZATION = "Authorization";

    private final UrlProperties urlProperties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Wit API")
                                .description("여행 동행 플랫폼 API 명세서")
                                .version("v1.0.0"))
                .servers(getSwaggerServers())
                .components(setAuth())
                .addSecurityItem(setSecurityRequirement());
    }

    private List<Server> getSwaggerServers() {
        return List.of(new Server().url(urlProperties.server()));
    }

    private Components setAuth() {
        return new Components()
                .addSecuritySchemes(
                        ACCESS_TOKEN,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name(AUTHORIZATION));
    }

    private SecurityRequirement setSecurityRequirement() {
        return new SecurityRequirement().addList(ACCESS_TOKEN);
    }
}
