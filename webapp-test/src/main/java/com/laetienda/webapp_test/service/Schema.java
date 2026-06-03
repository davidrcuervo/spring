package com.laetienda.webapp_test.service;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.webapp_test.repository.TestSchemaRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class Schema {
    private static final Logger log = LoggerFactory.getLogger(Schema.class);

    private TestUserDto[] users;

    private final TestSchemaRepo repo;
    private final UtilsBox utils;

    public Schema(
            TestSchemaRepo testSchemaRepo,
            UtilsBox utilsBox
    ) {
        this.repo = testSchemaRepo;
        this.utils = utilsBox;
    }

    public void run() throws HttpStatusCodeException, AssertionError {

        users = utils.getTestUsers(3, "webappTestSchema");
        final String groupName = "testGroup_TestSchemaApiImplementation";

        try{
            log.info("TEST_SCHEMA::run | Starting schema test ...");

            //CREATE
            ItemTypeA item = repo.createItem(
                    "testItem_TestSchemaApiImplementation",
                    45,
                    "Street 70B No. 87B - 24",
                    groupName,
                    DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY,
                    DbServiceAccessPolicy.NO_SERVICE,
                    users[1].getToken()
            );

            //FIND
            DbGroup group = repo.findByName(groupName, users[1].getToken());

            //UPDATE
            Map<String, String> params = Map.of("userAccessPolicy", DbUserAccessPolicy.MANAGE_BY_ALL.toString());
            group = repo.update(group.getId(), params, users[1].getToken());

            //ADD MEMBER
            group = repo.addMember(group.getId(), users[2].getUserId(), users[1].getToken());

            //REMOVE MEMBER
            group = repo.removeMember(
                    group.getId(),
                    groupName,
                    users[2].getUserId(), users[2].getToken(),
                    users[1].getToken()
            );

            //FIND ALL
            List<DbGroup> groups = repo.findAll(users[1].getToken());

            //DELETE ITEM
            repo.deleteItem(ItemTypeA.class, item.getId(), users[1].getToken());

            //FIND ORPHANS
            groups = repo.getOrphans(users[1].getToken());

            //DELETE GROUP
            repo.deleteGroup(group.getId(), groupName, users[1].getToken());

            this.getAllReadersAndEditors();

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

    private void getAllReadersAndEditors() throws AssertionError, HttpStatusCodeException{
        log.info("TEST_SCHEMA::getAllReadersAndEditors | Starting test....");

        ItemTypeA item1 = repo.createItem(
                "Test Schema 1 - Get All Readers And Editors",
                25,
                "101-1453 St. Laurent",
                null, null, null,
                null, null, null,
                users[1].getToken()
        );

        ItemTypeA item2 = repo.createItem(
                "Test Schema 2 - Get All Readers And Editors",
                35,
                "202-1453 St. Laurent",
                List.of(users[2].getUserId()),
                DbUserAccessPolicy.MANAGE_BY_ALL, DbServiceAccessPolicy.SERVICE_READ,
                null, null, null,
                users[1].getToken()
        );

        ItemTypeA item3 = repo.createItem(
                "Test Schema 3 - Get All Readers And Editors",
                45,
                "303-1453 St. Laurent",
                List.of(users[2].getUserId()),
                DbUserAccessPolicy.MANAGE_BY_ALL, DbServiceAccessPolicy.SERVICE_READ,
                List.of(users[3].getUserId()),
                DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY, DbServiceAccessPolicy.SERVICE_READ,
                users[1].getToken()
        );

        List<String> readers = repo.getReaders(item1.getId(), users[1].getToken());
        assertNotNull(readers);
        assertEquals(1, readers.size());

        List<String> editors = repo.getEditors(item3.getId(), users[3].getToken());
        assertNotNull(editors);
        assertEquals(2, editors.size());
        assertTrue(editors.contains(users[1].getUserId()));
        assertTrue(editors.contains(users[3].getUserId()));
        assertFalse(editors.contains(users[2].getUserId()));

        repo.deleteGroups(item1, users[1].getToken());
        repo.deleteItem(ItemTypeA.class, item1.getId(), users[1].getToken());

        repo.deleteGroups(item2, users[1].getToken());
        repo.deleteItem(ItemTypeA.class, item2.getId(), users[1].getToken());

        repo.deleteGroups(item3, users[1].getToken());
        repo.deleteItem(ItemTypeA.class, item3.getId(), users[1].getToken());

        log.info("TEST_SCHEMA::getAllReadersAndEditors | Test finish successfully!");
    }
}
