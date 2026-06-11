package com.laetienda.utils.lib;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.TestUserDto;
import org.springframework.web.client.HttpStatusCodeException;

public interface UtilsBox {
    String getCurrentUser() throws NotValidCustomException;
    TestUserDto[] getTestUsers(int numberOfUsers, String username) throws HttpStatusCodeException;
    void deleteTestUsers(TestUserDto[] testUsers) throws HttpStatusCodeException;

    TestUserDto getServiceUserDto();
}
