package com.laetienda.webapp_test.module;

import org.junit.jupiter.api.Test;

public interface UserModule {
    void setPort(int port);
    void testAuthentication();
    void testAuthenticationWithIvalidUsername();
    void testFindAll();
    void testFindAllUnautorized();
    void testFindByUsername();
    void testFindByUsernameRoleManager();
    void testFindByUsernameUnauthorized();
    void testFindByUsernameNotFound();
    void testUserCycle();
    void testCreateUserRepeatedUsername();
    void testCreateUserRepeatedEmail();
    void testCreateUserBadPassword();
    void testDeleteNotFound();
    void testDeleteUnauthorized();
    void testDeleteAdmuser();
    void testFindApplicationProfiles();
    void testApi();
    void deleteApiUser();
    void testCreateWithAuthenticatedUser();
    void login();
    void session();
}
