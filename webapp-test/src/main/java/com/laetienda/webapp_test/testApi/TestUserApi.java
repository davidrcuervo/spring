package com.laetienda.webapp_test.testApi;

import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.Usuario;
import org.springframework.web.client.HttpStatusCodeException;

public interface TestUserApi {
    KcUser create(Usuario usuario, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    Boolean userIdExists(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    String getToken(String username, String password) throws HttpStatusCodeException, AssertionError;
    void enable(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    void delete(String userId, String jwtToken, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
    String getEmailAddress(String userId, String clientRegistrationId) throws HttpStatusCodeException, AssertionError;
}
