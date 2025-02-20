package com.laetienda.usuerKc.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class UserKcSecurity {

    @Autowired private Environment env;

    @Bean
    public SecurityFilterChain userKcSecurityFilterChain(HttpSecurity http) throws Exception {
        String userPath = env.getProperty("api.usuario.folder");

        http.authorizeHttpRequests((authorize) -> {
            authorize.
                    requestMatchers(env.getProperty("api.usuario.test.path")).permitAll().
//                    requestMatchers(env.getProperty("api.usuario.login.path")).permitAll().
                    anyRequest().fullyAuthenticated();
        });
        http.oauth2ResourceServer((oath2) -> {
            oath2.jwt(Customizer.withDefaults());
        });
        return http.build();
    }
}