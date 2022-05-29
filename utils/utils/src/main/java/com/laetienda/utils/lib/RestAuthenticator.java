package com.laetienda.utils.lib;

import com.laetienda.lib.exception.CustomRestClientException;
import com.laetienda.lib.model.AuthCredentials;
import com.laetienda.model.user.GroupList;
import com.laetienda.utils.service.RestClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RestAuthenticator implements AuthenticationProvider {
    final private static Logger log = LoggerFactory.getLogger(RestAuthenticator.class);

    @Autowired
    private RestClientService restclient;

    @Value("${api.user.authenticate}")
    private String urlApiUserAuthenticate;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication result = null;
        AuthCredentials creds = new AuthCredentials(authentication.getName(), authentication.getCredentials().toString());

        log.trace("Authenticating user. $Username: {}", creds.getUsername());

        try {
            GroupList response = restclient.send(urlApiUserAuthenticate, HttpMethod.POST, creds, GroupList.class, null);
            List<GrantedAuthority> authorities = new ArrayList<>();

            if(response != null){
                response.getGroups().forEach((name, group) -> {
                    log.trace("User, ({}), role: {}", creds.getUsername(), group.getName());
                    authorities.add(new SimpleGrantedAuthority(group.getName()));
                });

            }else{
                throw new BadCredentialsException("Invalid password");
            }
            result = new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), authorities);
        }catch(CustomRestClientException e){
            e.getMistake().getErrors().forEach((key, value) -> {
                value.forEach((message) -> {
                    log.info("{}: {}", key, message);
                });
            });
        }

        //TEST hardcoded authentication
//        if(creds.getUsername().equals("admuser") && creds.getPassword().equals("secret")){
//            result = new UsernamePasswordAuthenticationToken(creds.getUsername(), creds.getPassword(), new ArrayList<>());
//        }

        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
