package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;

import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class ApiUserImplementation extends ApiRestClientImplementation implements ApiUser{
    private final static Logger log = LoggerFactory.getLogger(ApiUserImplementation.class);

    @Value("${kc.client-registration-id.webapp}")
    private String webappClientId;

    @Value("${api.kcUser.find.uri}")
    private String findUri;

    @Value("${api.kcUser.isUsernameValid.uri}")
    private String isUsernameValidUri;

    @Value("${api.kcUser.isUserIdValid.uri}")
    private String isUserIdValidUri;

    @Value("${api.kcUser.uri.userIdExists}")
    private String userIdExistsUri;

    @Value("${api.kcUser.find.byUserId.uri}")
    private String userByIdUri;

    @Value("${api.kcUser.uri.create}")
    private String createUri;

    @Value("${api.kcUser.uri.enable}")
    private String enableUri;

    @Value("${api.kcUser.uri.delete}")
    private String deleteUri;

    @Value("${api.kcUser.token.uri}")
    private String tokenUri;

    @Value("${api.kcUser.uri.findEmailAddress}")
    private String findEmailAddressUri;

    private final Environment env;
    private final ObjectMapper json;
    private final RestClient client;

    public ApiUserImplementation(
            RestClient restClient,
            Environment environment,
            ObjectMapper objectMapper,
            ToolBoxService toolBoxService
    ){
        super(restClient, toolBoxService);
        this.client = restClient;
        this.json = objectMapper;
        this.env = environment;
    }

    @Override
    public String isUsernameValid(String username) throws HttpStatusCodeException {
        log.debug("API_USER::isValidUser. $username: {} | $address: {}", username, isUsernameValidUri);
        return super.get(null, isUsernameValidUri, username);
    }

    @Override
    public String isUserIdValid(String userId) throws HttpStatusCodeException {
        log.debug("API_USER::isUserIdValid | $address: {}", isUserIdValidUri);
        return super.get(clientRegistrationId(webappClientId), isUserIdValidUri, userId);
    }

    @Override
    public void userIdExists(String userId, String clientRegistrationId) throws HttpStatusCodeException {
        log.debug("API_USER::userIdExists. $userId: {} | $address: {}", userId, userIdExistsUri);
        String response = super.get(
                clientRegistrationId(clientRegistrationId),
                userIdExistsUri, userId
        );
//        return Boolean.parseBoolean(response);
    }

    @Override
    public KcUser getCurrentUser() throws HttpStatusCodeException {
        log.debug("API_USER::getCurrentUser | ");

        try {
            String response = super.get(null, findUri);
            return json.readValue(response, KcUser.class);
        } catch (JsonProcessingException e) {
            log.error("API_USER::getCurrentUser. | $error: {}", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public KcUser getCurrentUserWithToken(String jwtToken) throws HttpStatusCodeException {
        log.debug("API_USER::getCurrentUser | $token: {}", jwtToken);

        try {
            String response = super.get(a -> a.put("jwtToken", jwtToken), findUri);
            return json.readValue(response, KcUser.class);
        } catch (JsonProcessingException e) {
            log.error("API_USER::getCurrentUser | $error: {}", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public KcUser getUserWithWebAppService(String userId) throws HttpStatusCodeException {
        log.debug("API_USER::getUserWithService | $userId: {} | $address: {}", userId, userByIdUri);
        return this.getUser(clientRegistrationId(webappClientId), userByIdUri, userId);
    }

    @Override
    public KcUser create(Usuario usuario, String clientRegistrationId) throws HttpStatusCodeException {
        log.debug("API_USER::create. $address: {}", createUri);

        try {
            String response = super.post(
                    json.writeValueAsString(usuario),
                    clientRegistrationId(clientRegistrationId),
                    createUri
            );

            return json.readValue(response, KcUser.class);
        } catch (JsonProcessingException e) {
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void enable(String userId, String clientRegistrationId) throws HttpStatusCodeException {
        log.debug("API_USER::enable. $userId: {} | $address: {}", userId, enableUri);
        super.put(
                clientRegistrationId(clientRegistrationId),
                enableUri, userId
        );
    }

    @Override
    public void delete(String userId, String jwtToken) throws HttpStatusCodeException {
        log.debug("API_USER::delete. $userId: {}, $address: {}", userId, deleteUri);

        super.delete(
                a -> a.put("jwtToken", jwtToken),
                deleteUri, userId
        );
    }

    @Override
    public String getToken(String username, String password) throws HttpStatusCodeException {
        log.debug("USER_API::getToken. $username: {} | $address: {}", username, tokenUri);

        MultiValueMap<String, String> credentials = new LinkedMultiValueMap<>();
        credentials.add("username",username);
        credentials.add("password",password);

        ResponseEntity<String> resp = client.post().uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(credentials)
                .retrieve().toEntity(String.class);
        log.trace("API_USER::getToken. $status: {}", resp.getStatusCode());
        log.trace("API_USER::getToken. $token: {}", resp.getBody());
        return resp.getBody();
    }

    @Override
    public String getCurrentUserId() throws HttpStatusCodeException{
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return isUserIdValid(userId);
    }

    @Override
    public String getEmailAddress(String userId, String clientRegistrationId) throws HttpStatusCodeException {
        log.debug("API_USER::getEmailAddress. $userId: {} | $address: {}", userId, findEmailAddressUri);

        return super.get(
                clientRegistrationId(clientRegistrationId),
                findEmailAddressUri, userId
        );
    }

    @Override
    public String getEmailAddress(String userId) throws HttpStatusCodeException {
        return super.get(null,  findEmailAddressUri, userId);
    }

    private KcUser getUser(
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException {
        return client.get().uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes == null ? a -> {} : attributes)
                .retrieve()
                .toEntity(KcUser.class)
                .getBody();
    }
}