package com.laetienda.utils.service.api;

import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.Usuario;
import org.springframework.web.client.HttpStatusCodeException;

public interface ApiUser extends ApiRestClient {
    String isUsernameValid(String username) throws HttpStatusCodeException;
    String isUserIdValid(String userId) throws HttpStatusCodeException;
    void userIdExists(String userId, String clientRegistrationId) throws HttpStatusCodeException;
    KcUser getCurrentUser() throws HttpStatusCodeException;
    KcUser getCurrentUserWithToken(String jwtToken) throws HttpStatusCodeException;
    KcUser getUserWithWebAppService(String userId) throws HttpStatusCodeException;
    KcUser create(Usuario usuario, String clientRegistrationId) throws HttpStatusCodeException;
    void enable(String userId, String clientRegistrationId) throws HttpStatusCodeException;
    void delete(String userId, String jwtToken) throws HttpStatusCodeException;
    String getToken(String username, String password) throws  HttpStatusCodeException;
    String getCurrentUserId() throws HttpStatusCodeException;
    String getEmailAddress(String userId, String clientRegistrationId) throws HttpStatusCodeException;
    String getEmailAddress(String userId) throws HttpStatusCodeException;

}
