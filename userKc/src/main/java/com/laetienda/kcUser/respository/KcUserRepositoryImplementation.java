package com.laetienda.kcUser.respository;

import com.laetienda.model.kc.KcToken;
import com.laetienda.model.kc.KcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Repository
public class KcUserRepositoryImplementation implements KcUserRepository {
    final private static Logger log = LoggerFactory.getLogger(KcUserRepositoryImplementation.class);

    final private RestClient client;
    @Autowired private Environment env;

    @Value("${kc.client-id}")
    private String clientId;

    KcUserRepositoryImplementation(RestClient restClient){
        this.client = restClient;
    }

    @Override
    public KcUser find() {
        String address = env.getProperty("api.kc.account", ""); //http://keycloaket:${PORT_KEYCLOAK}/realms/etrealm/account
        log.trace("KC_USER_REPOSITORY::find. $address: {}", address);

        return client.get().uri(address)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(KcUser.class).getBody();
    }

    @Override
    public KcToken getToken(MultiValueMap<String, String> creds) {
        String address = env.getProperty("api.kc.token");
        String clientId = env.getProperty("kc.user.client.id");
        String clientSecret = env.getProperty("kc.user.client.password");
        log.debug("USER_REPOSITORY::getToken. $username: {}, | $clientId: {}, | $address: {}", creds.getFirst("username"), clientId, address);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", creds.getFirst("username"));
        body.add("password", creds.getFirst("password"));

        return address == null ? null :
                client.post().uri(address)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(body)
                        .retrieve()
                        .toEntity(KcToken.class).getBody();
    }

    @Override
    public List<KcUser> findByUsername(String username) {
        String address = env.getProperty("api.kc.admin.user.byUsername", "/admin/master/users");
        log.debug("USER_REPOSITORY::findByUsername. $username: {} | clientId: {} | $address: {}", username, clientId, address);

        List<KcUser> result = client.get().uri(address, username)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(clientRegistrationId(clientId))
                .retrieve()
                .body(new ParameterizedTypeReference<List<KcUser>>() {});
        log.trace("USER_REPOSITORY::isValidUser. $result: {}", result != null && result.isEmpty() ? "null" : result.getFirst().getFullName());
        return result;
    }

    @Override
    public KcUser findByUserId(String userId) {
        String address = env.getProperty("api.kc.admin.user.byUserId", "/admin/master/users/{userId}");
        log.debug("USER_REPOSITORY::isUserIdValid $userId: {} | $clientId: {} | $address: {}", userId, clientId, address);

        try {
            return client.get().uri(address, userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .attributes(clientRegistrationId(clientId))
                    .retrieve().toEntity(KcUser.class).getBody();
        }catch (HttpClientErrorException | HttpServerErrorException e){
            log.warn(e.getMessage());
            log.trace(e.getMessage(), e);
            return null;
        }
    }
}