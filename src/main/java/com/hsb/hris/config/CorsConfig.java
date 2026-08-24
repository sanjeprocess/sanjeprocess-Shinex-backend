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
        cc.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        cc.setAllowedHeaders(Arrays.asList("*"));
        cc.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        cc.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/api/**", cc);
        src.registerCorsConfiguration("/swagger-ui/**", cc);
        src.registerCorsConfiguration("/v3/api-docs/**", cc);
        return new CorsFilter(src);
    }
}
