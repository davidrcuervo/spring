package com.laetienda.usuario.lib;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.*;


public class CustomLdapAuthenticationProvider implements AuthenticationProvider {
    final private static Logger log = LoggerFactory.getLogger(CustomLdapAuthenticationProvider.class);

    @Autowired
    private UserService uService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Usuario user = new Usuario();
        user.setUsername(authentication.getName());
        user.setPassword(authentication.getCredentials().toString());
        log.trace("AUTHENTICATION_PROVIDER::Authenticate. $username: {}", user.getUsername());

        try {
            List<GrantedAuthority> authorities = new ArrayList<>();
            GroupList groups = uService.authenticate(user);

            if(groups != null){
                groups.getGroups().forEach((key, group) -> {
                    String role = String.format("ROLE_%s", group.getName().toString().toUpperCase());
                    log.trace("User: {} -> has role: {}", authentication.getName(), role);
                    authorities.add(new SimpleGrantedAuthority(role));
                });

            }else{
                throw new BadCredentialsException("Invalid password");
            }

            Authentication result = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), authorities);
            return result;
        } catch (NotValidCustomException e) {
            throw new BadCredentialsException("Ivalid username");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
