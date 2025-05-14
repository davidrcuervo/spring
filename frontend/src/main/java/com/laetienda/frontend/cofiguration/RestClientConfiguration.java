package com.laetienda.frontend.cofiguration;

import com.laetienda.lib.service.CustomRestClient;
import com.laetienda.lib.service.CustomRestClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {
    final private static Logger log = LoggerFactory.getLogger(RestClientConfiguration.class);

    @Bean
    public RestClient restClient(
            RestClient.Builder builder,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {

        return builder.requestInterceptor(getCustomRestClient().oauth2Interceptor(authorizedClientManager)).build();
    }

    @Bean
    CustomRestClient getCustomRestClient() {
        return new CustomRestClientImpl();
    }
}

//    private ClientHttpRequestInterceptor oauth2Interceptor(
//            OAuth2AuthorizedClientManager authorizedClientManager
//    ){
//
//        return (request, body, execution) -> {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication instanceof OAuth2AuthenticationToken principal) {
//                String clientId = principal.getAuthorizedClientRegistrationId();
//                log.trace("RESTCLIENT_CONFIGURATION::intercept. $clientId: {}", clientId);
//
//                OAuth2AuthorizeRequest auth2AuthorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(clientId)
//                        .principal(authentication)
//                        .build();
//
//                OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(auth2AuthorizeRequest);
//
//                if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
//                    String token = authorizedClient.getAccessToken().getTokenValue();
//                    log.trace("RESTCLIENT_CONFIGURATION::intercept. $token: {}", token);
//                    request.getHeaders().setBearerAuth(token);
//                }
//            }
//
//            return execution.execute(request, body);
//        };
//    }
//
//    static class BearerTokenPropagationInterceptor implements ClientHttpRequestInterceptor {
//
//        private final OAuth2AuthorizedClientManager clientManager;
//        private final JwtDecoder jwtDecoder;
//
//        public BearerTokenPropagationInterceptor(
//                OAuth2AuthorizedClientManager clientManager,
//                JwtDecoder jwtDecoder) {
//            this.clientManager = clientManager;
//            this.jwtDecoder = jwtDecoder;
//        }
//
//        @Override
//        public ClientHttpResponse intercept(
//                HttpRequest request,
//                byte[] body,
//                ClientHttpRequestExecution execution)
//                throws IOException {
//
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if(authentication instanceof OAuth2AuthenticationToken principal){
//                String registrationId = principal.getAuthorizedClientRegistrationId();
//                log.trace("RESTCLIENT_CONFIGURATION::intercept. request contains authentication bearer token. $registrationId: {}", registrationId);
//
//                //Get authorized client
//                OAuth2AuthorizedClient authorizedClient = clientManager.authorize(
//                        OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
//                                .principal(principal)
//                                .build()
//                );
//
//                //find token and set it to header
//                if(authorizedClient != null){
//                    Jwt jwt = jwtDecoder.decode(authorizedClient.getAccessToken().getTokenValue());
//                    log.trace("RESTCLIENT_CONFIGURATION::intercept. Token added to authentication bearer header");
////                    log.trace("RESTCLIENT_CONFIGURATION::intercept. $jwt: {}", jwt.getTokenValue());
//                    request.getHeaders().setBearerAuth(jwt.getTokenValue());
//                }else{
//                    log.trace("RESTCLIENT_CONFIGURATION::intercept. no authorized client found");
//                }
//
//            }
//
//            return execution.execute(request, body);
//        }
//    }