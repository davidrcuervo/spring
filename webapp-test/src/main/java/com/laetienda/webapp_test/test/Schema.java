package com.laetienda.webapp_test.test;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.webapp_test.testApi.TestSchemaApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

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
        final String groupName = "testGroup_TestSchemaApiImplementation";

        try{
            log.info("TEST_SCHEMA::run | Starting schema test ...");

            //CREATE
            ItemTypeA item = testSchemaApi.createItem(
                    "testItem_TestSchemaApiImplementation",
                    45,
                    "Street 70B No. 87B - 24",
                    groupName,
                    DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY,
                    DbServiceAccessPolicy.NO_SERVICE,
                    users[1].getToken()
            );

            //FIND
            DbGroup group = testSchemaApi.findByName(groupName, users[1].getToken());

            //UPDATE
            Map<String, String> params = Map.of("userAccessPolicy", DbUserAccessPolicy.MANAGE_BY_ALL.toString());
            group = testSchemaApi.update(group.getId(), params, users[1].getToken());

            //ADD MEMBER
            group = testSchemaApi.addMember(group.getId(), users[2].getUserId(), users[1].getToken());

            //REMOVE MEMBER
            group = testSchemaApi.removeMember(group.getId(), users[2].getUserId(), groupName, users[1].getToken());

            //FIND ALL
            List<DbGroup> groups = testSchemaApi.findAll(users[1].getToken());

            //DELETE ITEM
            testSchemaApi.deleteItem(ItemTypeA.class, item.getId(), users[1].getToken());

            //FIND ORPHANS
            groups = testSchemaApi.getOrphans(users[1].getToken());

            //DELETE GROUP
            testSchemaApi.deleteGroup(group.getId(), groupName, users[1].getToken());

            log.info("TEST_SCHEMA::run | Schema test finish successfully!!!");

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
