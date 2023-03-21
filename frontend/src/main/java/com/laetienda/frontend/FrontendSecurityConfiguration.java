package com.laetienda.frontend;

import com.laetienda.utils.lib.CustomRestAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class FrontendSecurityConfiguration extends WebSecurityConfigurerAdapter {

//    @Value("${ldap.url}")
//    private String ldapUrl;
//
//    @Value("${ldap.user.dn.pattern}")
//    private String ldapUserDnPattern;
//
//    @Value("${ldap.group.search.base}")
//    private String ldapGroupSearchBase;
//
//    @Value("ldap.password.attribute")
//    private String ldapPasswordAttribute;

    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http
                .authorizeRequests()
                    .antMatchers("/anonymous*").anonymous()
                    .antMatchers("/home", "/", "/home.html", "/index", "/index.html", "/user/signup.html").permitAll()
                    .antMatchers("/bootstrap/**", "/styles/**", "/scripts/**").permitAll()
                    .antMatchers("/login*").permitAll()
                    .anyRequest().authenticated()
                    .and()
                .formLogin()
                    .loginPage("/login.html")
                    .loginProcessingUrl("/perform_login")
                    .defaultSuccessUrl("/", false);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
        auth.authenticationProvider(getAuthenticationProvider());
//        auth.ldapAuthentication()
//                .userDnPatterns(ldapUserDnPattern) //"uid={0},ou=people"
//                .groupSearchBase(ldapGroupSearchBase) //"ou=groups"
//                .contextSource()
//                .url(ldapUrl) //ldap://localhost:389/dc=example,dc=com
//                .and()
//                .passwordCompare()
//                .passwordEncoder(passwordEncoder())
//                .passwordAttribute(ldapPasswordAttribute); //userPassword
    }

    @Bean
    public AuthenticationProvider getAuthenticationProvider(){
        return new CustomRestAuthenticationProvider();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
                return new BCryptPasswordEncoder();

        //Copied from stackoverflow
//        final LdapShaPasswordEncoder sha = new LdapShaPasswordEncoder();
//            return new PasswordEncoder() {
//                @Override
//                public String encode(CharSequence rawPassword) {
//                    return sha.encodePassword(rawPassword.toString(), null);
//                }
//                @Override
//                public boolean matches(CharSequence rawPassword, String encodedPassword) {
//                    return sha.isPasswordValid(encodedPassword, rawPassword.toString(), null);
//                }
//            };
    }

//    @Bean
//    public CookieSerializer getCookieSerializer(){
//        DefaultCookieSerializer result = new DefaultCookieSerializer();
//        result.setUseBase64Encoding(false);
//        return result;
//    }
}
