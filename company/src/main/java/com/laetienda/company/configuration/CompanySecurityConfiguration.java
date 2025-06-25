package com.laetienda.company.configuration;

import com.laetienda.lib.service.KeycloakGrantedAuthoritiesConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class CompanySecurityConfiguration {

    @Autowired private Environment env;

    @Bean
    public SecurityFilterChain companySecurityFilter(HttpSecurity http) throws Exception {
        String actuator = String.format("%s/*", env.getProperty("api.actuator.folder"));

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

        http.sessionManagement(sessions -> {
            sessions.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        });

        http.csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public KeycloakGrantedAuthoritiesConverter kcAuthoritiesConverter() {
        return new KeycloakGrantedAuthoritiesConverter();
    }
}
