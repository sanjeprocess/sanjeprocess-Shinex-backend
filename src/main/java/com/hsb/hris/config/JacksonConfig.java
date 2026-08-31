package com.hsb.hris.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            builder.deserializerByType(String.class, new JsonDeserializer<String>() {
                @Override
                public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String value = p.getValueAsString();
                    return value != null ? value.trim() : null;
                }
            });

            builder.serializerByType(String.class, new JsonSerializer<String>() {
                @Override
                public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    if (value == null) {
                        gen.writeNull();
                    } else {
                        gen.writeString(value.trim());
                    }
                }
            });
        };
    }
}
