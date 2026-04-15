package com.laetienda.utils.lib;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.model.user.Usuario;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

@Component
public class UtilsBoxImplementation implements UtilsBox{
    private final static Logger log = LoggerFactory.getLogger(UtilsBoxImplementation.class);

    private final ApiUser apiUser;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${kc.client-registration-id.webapp}")
    private String clientRegistrationId;

    @Value("${api.kc.realm.certs}")
    private String kcCerts;

    public UtilsBoxImplementation(
            ApiUser apiUser,
            OAuth2AuthorizedClientManager authorizedClientManager
    ) {
        this.apiUser = apiUser;
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public String getCurrentUser() throws NotValidCustomException{
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.trace("UTILS_BOX::getCurrentUser. $loggedUser: {}", userId);

        apiUser.isUserIdValid(userId);

        return userId;
    }

    @Override
    public TestUserDto[] getTestUsers(int numberOfUsers, String username) throws HttpStatusCodeException {

        TestUserDto[] result = new TestUserDto[numberOfUsers + 1];
        String[] firstName = {"First", "Second", "Third", "Forth",  "Fifth", "Sixth"};

        for(int j = 1; j <= numberOfUsers; j++) {
            String u = String.format("testUser%d_%s", j, username);
            Usuario user = new Usuario(u,
                    firstName[j], null, username,
                    u+"@address.com", false,
                    "secreteTestPassword"+j, "secreteTestPassword"+j
            );

            KcUser kcUser = apiUser.create(user, clientRegistrationId);
            apiUser.enable(kcUser.getId(), clientRegistrationId);
            String token = apiUser.getToken(user.getUsername(), user.getPassword());

            result[j] = new TestUserDto(kcUser.getId(), token);
        }

        //SET SERVICE ACCOUNT IN USERS ARRAY
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal("test-system") // Identity of the requester
                .build();

        // This triggers the POST to Keycloak if the token is missing or expired
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

        if(authorizedClient == null || authorizedClient.getAccessToken() == null) {
           throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "authorized client not found");

        }else{
            String serviceAccountToken = authorizedClient.getAccessToken().getTokenValue();
            Jwt jwt = NimbusJwtDecoder.withJwkSetUri(kcCerts).build().decode(serviceAccountToken);

            result[0] = new TestUserDto(jwt.getSubject(), serviceAccountToken);
        }

        return result;
    }

    @Override
    public void deleteTestUsers(TestUserDto[] testUsers) throws HttpStatusCodeException {
        for(int j = 1; j < testUsers.length; j++) {
            apiUser.delete(testUsers[j].userId, testUsers[j].getToken());
        }
    }
}
