package com.laetienda.webapp_test.test;

import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.Usuario;
import com.laetienda.webapp_test.testApi.TestUserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class User {
    private final static Logger log = LoggerFactory.getLogger(User.class);

    private final TestUserApi testUserApi;

    @Value("${kc.client-registration-id.webapp}")
    private String clientRegistrationId;

    public User(TestUserApi testUserApi) {
        this.testUserApi = testUserApi;
    }

    public void run() throws HttpStatusCodeException, AssertionError {
        log.info("TEST_USER::run | Starting user test....");

        final String password = "secretPassword";

        Usuario usuario = new Usuario("testUserApiImplementation",
                "User", "Api", "Test",
                "testuserapiimplementation@mail.com", false,
                password, password
        );

        //SUCCESSFUL: Create new user
        KcUser user = testUserApi.create(usuario, clientRegistrationId);
        Boolean flag = testUserApi.userIdExists(user.getId(), clientRegistrationId);
        assertTrue(flag);

        //FORBIDDEN: Create same user twice
        HttpStatusCodeException e = assertThrows(
                HttpStatusCodeException.class,
                () -> testUserApi.create(usuario, clientRegistrationId)
        );
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());

        //GET_TOKEN::BAD_REQUEST.
        e =  assertThrows(
                HttpStatusCodeException.class,
                () -> testUserApi.getToken(user.getUsername(), password)
        );
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());

        //ENABLE::SUCCESSFUL
        testUserApi.enable(user.getId(), clientRegistrationId);
        String jwtToken = testUserApi.getToken(user.getUsername(), password);
        testUserApi.getEmailAddress(user.getId(), clientRegistrationId);

        //SUCCESSFUL: Delete user
        testUserApi.delete(user.getId(), jwtToken, clientRegistrationId);

        log.info("TEST_USER::run | Finished Successfully user test!!!");
    }
}
