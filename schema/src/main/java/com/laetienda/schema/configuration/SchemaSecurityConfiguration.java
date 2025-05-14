package com.laetienda.schema.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.authentication.AuthenticationManager;
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
public class SchemaSecurityConfiguration {
    private final static Logger log = LoggerFactory.getLogger(SchemaSecurityConfiguration.class);

    @Autowired
    private Environment env;

    @Bean
    public SecurityFilterChain schemaSecurityFilterChain(HttpSecurity http) throws Exception{
        String rootPath = env.getProperty("api.schema.root");
        http
                .sessionManagement((httpSecuritySessionManagementConfigurer) -> {
                    httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.ALWAYS);
                })
                .authorizeHttpRequests((requests) -> {
                    requests
                            .requestMatchers(rootPath + "/" + env.getProperty("api.schema.helloAll")).permitAll()
                            .requestMatchers(rootPath + "/" + env.getProperty("api.schema.helloUser")).authenticated()
                            .anyRequest().hasRole("VALIDUSERACCOUNTS");
                })
                .httpBasic(withDefaults())
                .csrf((csrf) -> {
                    csrf.disable();
                });
        return http.build();
    }

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
