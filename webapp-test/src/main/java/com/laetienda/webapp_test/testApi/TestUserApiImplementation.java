package com.laetienda.webapp_test.testApi;

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
public class TestUserApiImplementation implements TestUserApi {
    private final static Logger log = LoggerFactory.getLogger(TestUserApiImplementation.class);

    final private ApiUser apiUser;

    public TestUserApiImplementation(ApiUser apiUser) {
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
    public Boolean userIdExists(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::userIdExists | $userId: {}", userId);
        apiUser.userIdExists(userId, clientRegistrationId);
        return true;
    }

    @Override
    public String getToken(String username, String password) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::getToken | $username: {}", username);
        return apiUser.getToken(username, password);
    }

    @Override
    public void enable(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::enable | $userId: {}", userId);
        apiUser.enable(userId, clientRegistrationId);
    }

    @Override
    public String getEmailAddress(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_USER::getEmailAddress | $userId: {}", userId);

        HttpStatusCodeException e = assertThrows(
                HttpStatusCodeException.class,
                () -> apiUser.getEmailAddress(userId)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());

        return apiUser.getEmailAddress(userId, clientRegistrationId);
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
