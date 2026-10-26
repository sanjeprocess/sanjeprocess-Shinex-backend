package com.hsb.hris.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Value("${cors.origins:http://localhost:5173,https://hris.process361.com}")
    private String corsOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cc = new CorsConfiguration();
        cc.setAllowedOriginPatterns(Arrays.stream(corsOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        cc.setAllowedHeaders(Arrays.asList("*"));
        cc.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cc.setAllowCredentials(true);
        cc.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cc);
        src.registerCorsConfiguration("/api/**", cc);
        src.registerCorsConfiguration("/swagger-ui/**", cc);
        src.registerCorsConfiguration("/v3/api-docs/**", cc);
        return new CorsFilter(src);
    }
}
