package com.laetienda.usuario.configuration;

import com.laetienda.usuario.lib.CustomLdapAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class UsuarioSecurityConfiguration /*extends WebSecurityConfigurerAdapter*/ {
    private final static Logger log = LoggerFactory.getLogger(UsuarioSecurityConfiguration.class);

//    @Autowired
//    CustomLdapAuthenticationProvider customLdapAuthenticationProvider;

    @Bean
    public SecurityFilterChain usuarioSecurityFilterChain(HttpSecurity http) throws Exception{
        http
                .sessionManagement(
                        httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .authorizeHttpRequests((requests) ->
                        requests.
//                                requestMatchers("/api/v0/group/helloword.html").permitAll().
//                                requestMatchers("/anonymous*").anonymous().
                                requestMatchers("/api/v0/user/home.html").authenticated().
                                requestMatchers("/api/v0/user/authenticate.html").permitAll().
                                requestMatchers("/api/v0/user/create.html").permitAll().
                                requestMatchers("/api/v0/user/requestpasswordrecovery.html").permitAll().
                                requestMatchers("/api/v0/user/passwordrecovery.html").permitAll().
                                requestMatchers("/api/v0/user/emailvalidation.html").authenticated().
                                requestMatchers("/api/v0/user/update.html").authenticated().
                                requestMatchers("/api/v0/user/delete.html").authenticated().
                                requestMatchers("/api/v0/user/auth").authenticated().
                                anyRequest().hasRole("VALIDUSERACCOUNTS")
                        )
                .httpBasic(withDefaults())
                .csrf((csrf) -> {
                    csrf.disable();
                });
        return http.build();
    }


//    @Autowired
//    public void registerProvider(AuthenticationManagerBuilder auth){
//        auth.authenticationProvider(customLdapAuthenticationProvider);
//    }

    @Bean
    LdapAuthoritiesPopulator authorities(BaseLdapPathContextSource contextSource) {
        String groupSearchBase = "ou=wroups";
        DefaultLdapAuthoritiesPopulator authorities =
                new DefaultLdapAuthoritiesPopulator(contextSource, groupSearchBase);
        authorities.setGroupSearchFilter("uniqueMember={0}");
        authorities.setGroupRoleAttribute("cn");
        return authorities;
    }

    @Bean
    AuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource, LdapAuthoritiesPopulator authorities) {
        LdapBindAuthenticationManagerFactory factory = new LdapBindAuthenticationManagerFactory(contextSource);
        factory.setUserDnPatterns("uid={0},ou=people");
        factory.setLdapAuthoritiesPopulator(authorities);
        return factory.createAuthenticationManager();
    }
}