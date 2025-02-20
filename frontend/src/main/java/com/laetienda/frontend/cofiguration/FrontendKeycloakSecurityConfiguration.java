package com.laetienda.frontend.cofiguration;

import com.laetienda.utils.lib.CustomRestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class FrontendKeycloakSecurityConfiguration {
    private final static Logger log = LoggerFactory.getLogger(FrontendKeycloakSecurityConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy(){
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository){
        OidcClientInitiatedLogoutSuccessHandler successHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        successHandler.setPostLogoutRedirectUri(URI.create(env.getProperty("api.frontend.home")).toString());
        return successHandler;
    }

    @Bean
    public SecurityFilterChain frontendSecurityFilterChain(HttpSecurity http, OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler) throws Exception{
        http.authorizeHttpRequests((requests) ->
                requests.
                        requestMatchers("/anonymous*").anonymous().
                        requestMatchers("/home.html").permitAll().
                        requestMatchers("/home", "/", "/home.html", "/index", "/index.html", "/user/signup.html").permitAll().
                        requestMatchers("/bootstrap/**", "/styles/**", "/scripts/**").permitAll().
                        requestMatchers("/login*").permitAll().
                        requestMatchers("/manage/*").hasAuthority("role_manager").
                        anyRequest().authenticated()
                );
        http.oauth2Login(Customizer.withDefaults())
                .logout((logout) -> {
                    logout.logoutSuccessHandler(oidcLogoutSuccessHandler);
                });

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper(){
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<GrantedAuthority>();

            authorities.forEach(authority -> {
                if(OidcUserAuthority.class.isInstance(authority)){

                    OidcUserAuthority oidcUserAuthority = (OidcUserAuthority)authority;
                    OidcIdToken idToken = oidcUserAuthority.getIdToken();
                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();

                    Map<String, Object> realmAccess = userInfo.getClaim("realm_access");

                    if(realmAccess != null){
                        Collection<String> realmRoles = (Collection<String>)realmAccess.get("roles");
                        if(realmRoles != null){

                            realmRoles.forEach((role) -> {
                                log.debug("FRONTEND_SECURITY::Config token contains role. $role: {}", role);
                                mappedAuthorities.add(new SimpleGrantedAuthority(role));
                            });

                        }else{
                            log.error("FRONTEND_SECURITY::Config Keycloak token does not contain any realm roles");
                        }
                    }else{
                        log.error("FRONTEND_SECURITY::Config Keycloak token does not contain realm access");
                    }
                }
            });

            return mappedAuthorities;
        };
    }
}
