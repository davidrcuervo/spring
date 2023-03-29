package com.laetienda.frontend;

import com.laetienda.utils.lib.CustomRestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class FrontendSecurityConfiguration {

    @Autowired
    public CustomRestAuthenticationProvider customRestAuthenticationProvider;

    @Bean
    public SecurityFilterChain frontendSecurityFilterChain(HttpSecurity http) throws Exception{
        http.authorizeHttpRequests((requests) ->
                requests.
                        requestMatchers("/anonymous*").anonymous().
                        requestMatchers("/home.html").permitAll().
                        requestMatchers("/home", "/", "/home.html", "/index", "/index.html", "/user/signup.html").permitAll().
                        requestMatchers("/bootstrap/**", "/styles/**", "/scripts/**").permitAll().
                        requestMatchers("/login*").permitAll().
                        anyRequest().authenticated()
                ).formLogin()
                .loginPage("/login.html")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/", false);

        return http.build();
    }

    @Autowired
    public void registerProvider(AuthenticationManagerBuilder auth) throws Exception{
        auth.authenticationProvider(customRestAuthenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
                return new BCryptPasswordEncoder();
    }
}
