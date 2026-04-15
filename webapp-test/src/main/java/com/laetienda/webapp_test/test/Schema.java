package com.laetienda.webapp_test.test;

import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.service.api.ApiUser;
import com.laetienda.webapp_test.testApi.TestSchemaApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class Schema {
    private static final Logger log = LoggerFactory.getLogger(Schema.class);

    private final TestSchemaApi testSchemaApi;
    private final UtilsBox utils;

    public Schema(
            TestSchemaApi testSchemaApi,
            UtilsBox utilsBox
    ) {
        this.testSchemaApi = testSchemaApi;
        this.utils = utilsBox;
    }

    public void run() throws HttpStatusCodeException, AssertionError {

        TestUserDto[] users = utils.getTestUsers(2, "webappTestSchema");

        try{
            testSchemaApi.dbGroups(users);

        }catch(HttpStatusCodeException ex){
            log.debug("SCHEMA_TEST::run $exception: {} | $code: {} | $error: {}", ex.getClass().getSimpleName(), ex.getStatusCode(), ex.getMessage());
            throw ex;

        }catch(AssertionError | Exception ex){
            log.debug("SCHEMA_TEST::run $exception: {} | $error: {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;

        } finally {
            utils.deleteTestUsers(users);
        }
    }
}
