package com.laetienda.lib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CustomRestClientImpl implements CustomRestClient{
    private static final Logger log = LoggerFactory.getLogger(CustomRestClientImpl.class);

    public ClientHttpRequestInterceptor oauth2Interceptor(
            OAuth2AuthorizedClientManager authorizedClientManager
    ){

        return (request, body, execution) -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof OAuth2AuthenticationToken principal) {
                String clientId = principal.getAuthorizedClientRegistrationId();
                log.trace("RESTCLIENT_CONFIGURATION::intercept. $clientId: {}", clientId);

                OAuth2AuthorizeRequest auth2AuthorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientId)
                        .principal(authentication)
                        .build();

                OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(auth2AuthorizeRequest);

                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                    String token = authorizedClient.getAccessToken().getTokenValue();
                    log.trace("RESTCLIENT_CONFIGURATION::intercept. $token: {}", token);
                    request.getHeaders().setBearerAuth(token);
                }
            }else if(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken){
                log.trace("RESTCLIENT_CONFIGURATION::intercept. token instance of JwtAuthenticationToken");

                Jwt jwt = jwtAuthenticationToken.getToken();
                request.getHeaders().setBearerAuth(jwt.getTokenValue());
            }else{
                log.debug("RESTCLIENT_CONFIGURATION::intercept. Token is not instance of any token");
            }

            return execution.execute(request, body);
        };
    }
}
