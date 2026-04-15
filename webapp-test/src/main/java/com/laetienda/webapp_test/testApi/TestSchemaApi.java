package com.laetienda.webapp_test.testApi;

import com.laetienda.model.user.TestUserDto;
import org.springframework.web.client.HttpStatusCodeException;

public interface TestSchemaApi {
    void dbItem() throws HttpStatusCodeException;
    void dbGroups(TestUserDto[] users) throws HttpStatusCodeException;
}
