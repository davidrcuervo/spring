package com.laetienda.usuario.configuration;

import com.laetienda.usuario.lib.CustomLdapAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class UsuarioSecurityConfiguration /*extends WebSecurityConfigurerAdapter*/ {

    @Autowired
    CustomLdapAuthenticationProvider customLdapAuthenticationProvider;

    @Bean
    public SecurityFilterChain usuarioSecurityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeRequests((requests) ->
                        requests.
                                requestMatchers("/anonymous*").anonymous().
                                requestMatchers("/api/v0/user/authenticate.html").permitAll().
                                requestMatchers("/api/v0/user/create.html").permitAll().
                                anyRequest().authenticated()
                        )
//                .and()
                .httpBasic()
                .and()
                .csrf().disable();
        return http.build();
    }


    @Autowired
    public void registerProvider(AuthenticationManagerBuilder auth){
        auth.authenticationProvider(customLdapAuthenticationProvider);
    }

    //    BEFORE Spring Security 5.7.0
//    @Autowired
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.authenticationProvider(getAuthenticationProvider());
////        auth.authenticationProvider(new CustomLdapAuthenticationProvider());
//    }

}
