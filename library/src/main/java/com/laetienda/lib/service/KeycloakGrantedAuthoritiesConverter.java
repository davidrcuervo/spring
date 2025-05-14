package com.laetienda.lib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private final static Logger log = LoggerFactory.getLogger(KeycloakGrantedAuthoritiesConverter.class);

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt){
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        return new JwtAuthenticationToken(jwt, getKcRealmRoles(realmAccess));
    }

    public Set<GrantedAuthority> getKcRealmRoles(Map<String, Object> realm){
        Set<GrantedAuthority> result = new HashSet<GrantedAuthority>();

        if(realm.get("roles") != null){
            Collection<String> realmRoles = (Collection<String>)realm.get("roles");

            realmRoles.forEach(role -> {
                log.trace("KC_JWT_CONVERVERT::getKcRealmRoles. $role: {}", role);
                result.add(new SimpleGrantedAuthority(role));
            });
        }else{
            log.warn("KC_JWT_CONVERVERT::getKcRealmRoles. Jwt does not contain realm roles");
        }

        return result;
    }
}
