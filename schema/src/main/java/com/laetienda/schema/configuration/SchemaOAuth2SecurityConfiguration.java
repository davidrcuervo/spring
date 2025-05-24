package com.laetienda.schema.configuration;

import com.laetienda.lib.service.KeycloakGrantedAuthoritiesConverter;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SchemaOAuth2SecurityConfiguration {
    private final static Logger log = LoggerFactory.getLogger(SchemaOAuth2SecurityConfiguration.class);

    @Autowired private Environment env;

    @Bean
    public SecurityFilterChain schemaSecurityFilter(@NotNull HttpSecurity http) throws Exception {
        String actuator = String.format("%s/*", env.getProperty("api.actuator.folder", "/actuator"));

        http.authorizeHttpRequests(authorize -> {
            authorize
                    .requestMatchers(actuator).permitAll()
                    .anyRequest().fullyAuthenticated();
        });

        http.oauth2ResourceServer(oauth2 -> {
            oauth2.jwt(jwtDecoder -> {
                jwtDecoder.jwtAuthenticationConverter(kcAuthoritiesConverter());
            });
        });

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public KeycloakGrantedAuthoritiesConverter kcAuthoritiesConverter(){
        return new KeycloakGrantedAuthoritiesConverter();
    }
}
