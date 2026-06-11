package com.laetienda.webapp_test.service;

import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.model.user.Usuario;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.webapp_test.repository.TestUserRepo;
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

    @Value("${kc.client-registration-id.webapp}")
    private String clientRegistrationId;

    private final TestUserRepo testUserRepo;

    public User(
            TestUserRepo testUserRepo
    ) {
        this.testUserRepo = testUserRepo;
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
        KcUser user = testUserRepo.create(usuario, clientRegistrationId);
        testUserRepo.userIdExists(user.getId(), clientRegistrationId);

        //FORBIDDEN: Create same user twice
        HttpStatusCodeException e = assertThrows(
                HttpStatusCodeException.class,
                () -> testUserRepo.create(usuario, clientRegistrationId)
        );
        assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());

        //ENABLE::SUCCESSFUL
        testUserRepo.enable(user.getId(), user.getUsername(), password, clientRegistrationId);
        String jwtToken = testUserRepo.getToken(user.getUsername(), password);
        testUserRepo.getEmailAddress(user.getId(), clientRegistrationId);
        user = testUserRepo.getUserWithToken(jwtToken);
        user = testUserRepo.getUserWithClientId(user.getId());

        //SUCCESSFUL: Delete user
        testUserRepo.delete(user.getId(), jwtToken, clientRegistrationId);

        log.info("TEST_USER::run | Finished Successfully user test!!!");
    }
}
