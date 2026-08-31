package com.hsb.hris.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cc = new CorsConfiguration();
        cc.setAllowedOriginPatterns(Arrays.asList("*"));
        cc.setAllowedHeaders(Arrays.asList("*"));
        cc.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cc.setAllowCredentials(true);
        cc.setExposedHeaders(Arrays.asList("Authorization"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/api/**", cc);
        src.registerCorsConfiguration("/swagger-ui/**", cc);
        src.registerCorsConfiguration("/v3/api-docs/**", cc);
        return new CorsFilter(src);
    }
}
