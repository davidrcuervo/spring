package com.laetienda.messenger.configuration;

import com.laetienda.utils.lib.CustomRestAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MessengerSecurityConfiguration {

    @Autowired
    public CustomRestAuthenticationProvider customRestAuthenticationProvider;

    @Bean
    public SecurityFilterChain messengerSecurityFilterChain(HttpSecurity http) throws Exception{
        http
            .authorizeRequests((requests) ->
                    requests.
                            requestMatchers("/api/v0/email/holaMundo").permitAll().
                            requestMatchers("/api/v0/email/preview/template").permitAll().
                            requestMatchers("/api/v0/email/sendMessage").permitAll().
                            requestMatchers("/api/v0/email/testSimplePost").permitAll().
//                            anyRequest().authenticated()
                            anyRequest().hasRole("VALIDUSERACCOUNTS")
            )
                .httpBasic()
                .and()
                .csrf().disable();
        return http.build();
    }

    @Autowired
    protected void registerProvider(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(customRestAuthenticationProvider);
    }
}
