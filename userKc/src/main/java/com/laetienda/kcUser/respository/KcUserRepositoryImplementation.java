package com.laetienda.kcUser.respository;

import com.laetienda.model.kc.KcToken;
import com.laetienda.model.kc.KcUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import org.springframework.http.HttpHeaders;

@Repository
public class KcUserRepositoryImplementation implements KcUserRepository {
    final private static Logger log = LoggerFactory.getLogger(KcUserRepositoryImplementation.class);

    final private RestClient client;
    @Autowired private Environment env;

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
    public String isValidUser(String username) {
        String address = env.getProperty("api.kc.admin.user", "/admin/master/users");
        String clientId = env.getProperty("spring.security.oauth2.client.registration.keycloak.client-id", "null");
        log.debug("USER_REPOSITORY::isValidUser. $username: {} | clientId: {} | $address: {}", username, clientId, address);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("username", username);

        String result = client.get().uri(address, username)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId(clientId))
                .retrieve()
                .toEntity(String.class).getBody();

        return null;
    }
}
