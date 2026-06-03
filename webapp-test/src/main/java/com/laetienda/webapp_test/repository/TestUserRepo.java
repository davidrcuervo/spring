package com.laetienda.webapp_test.repository;

import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.Usuario;
import org.springframework.web.client.HttpStatusCodeException;

public interface TestUserRepo {
    KcUser create(Usuario usuario, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    KcUser getUserWithToken(String token) throws HttpStatusCodeException;
    KcUser getUserWithClientId(String userId) throws HttpStatusCodeException;
    String getToken(String username, String password) throws HttpStatusCodeException, AssertionError;
    void enable(String userId, String username, String password, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    void userIdExists(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    void delete(String userId, String jwtToken, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    void getEmailAddress(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
}
