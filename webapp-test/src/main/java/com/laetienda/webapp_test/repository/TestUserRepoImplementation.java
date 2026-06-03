package com.laetienda.webapp_test.repository;

import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.Usuario;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class TestUserRepoImplementation implements TestUserRepo {
    private final static Logger log = LoggerFactory.getLogger(TestUserRepoImplementation.class);

    final private ApiUser apiUser;

    public TestUserRepoImplementation(ApiUser apiUser) {
        this.apiUser = apiUser;
    }

    @Override
    public KcUser create(Usuario usuario, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::create | $username: {}", usuario != null ? usuario.getUsername() : "null");
        KcUser result = apiUser.create(usuario, clientRegistrationId);
        assertNotNull(result);
        return result;
    }

    @Override
    public KcUser getUserWithToken(String token) throws HttpStatusCodeException {
        log.debug("TEST_USER::getUserWithToken | $token: private");
        KcUser result = apiUser.getCurrentUserWithToken(token);
        assertNotNull(result);
        return result;
    }

    @Override
    public KcUser getUserWithClientId(String userId) throws HttpStatusCodeException {
        log.debug("TEST_USER::getUserWithClientId | $userId: {}", userId);

        KcUser result = apiUser.getUserWithWebAppService(userId);
        assertNotNull(result);
        assertEquals(userId, result.getId());

        return result;
    }

    @Override
    public void userIdExists(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::userIdExists | $userId: {}", userId);
        apiUser.userIdExists(userId, clientRegistrationId);
    }

    @Override
    public String getToken(String username, String password) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::getToken | $username: {}", username);
        return apiUser.getToken(username, password);
    }

    @Override
    public void enable(String userId, String username, String password, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::enable | $userId: {}", userId);

        //GET_TOKEN::BAD_REQUEST.
        HttpStatusCodeException e =  assertThrows(
                HttpStatusCodeException.class,
                () -> getToken(username, password)
        );
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

        apiUser.enable(userId, clientRegistrationId);
    }

    @Override
    public void getEmailAddress(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::getEmailAddress | $userId: {}", userId);

        HttpStatusCodeException e = assertThrows(
                HttpStatusCodeException.class,
                () -> apiUser.getEmailAddress(userId)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        String result = apiUser.getEmailAddress(userId, clientRegistrationId);
        assertNotNull(result);
        assertTrue(
                result.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"),
                "getEmailAddress is no a valid email address"
        );
    }

    @Override
    public void delete(String userId, String jwtToken, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::delete | $userId: {}", userId);
        apiUser.delete(userId, jwtToken);

        //NOT FOUND: Find if user exists
        HttpStatusCodeException e = assertThrows(
                HttpStatusCodeException.class,
                () -> userIdExists(userId, clientRegistrationId)
        );
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
}
