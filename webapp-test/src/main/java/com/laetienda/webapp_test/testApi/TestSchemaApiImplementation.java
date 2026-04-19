package com.laetienda.webapp_test.testApi;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class TestSchemaApiImplementation implements TestSchemaApi {
    private final static Logger log = LoggerFactory.getLogger(TestSchemaApiImplementation.class);

    private final ApiSchemaGroup apiGroup;
    private final ApiSchema apiSchema;

    public TestSchemaApiImplementation(
            ApiSchema apiSchema,
            ApiSchemaGroup apiSchemaGroup
    ) {
        this.apiSchema = apiSchema;
        this.apiGroup = apiSchemaGroup;
    }


    @Override
    public void dbItem() throws HttpStatusCodeException, AssertionError {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public void dbGroups(TestUserDto[] users) throws HttpStatusCodeException {
        log.info("SCHEMA_TEST::groups | Test stating...");

        ItemTypeA item = new ItemTypeA(
                "testItem_TestSchemaApiImplementation",
                45,
                "Street 70B No. 87B - 24");

        final DbGroup groupFinal = new DbGroup("testGroup_TestSchemaApiImplementation");
        groupFinal.setUserAccessPolicy(DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY);
        groupFinal.setServiceAccessPolicy(DbServiceAccessPolicy.NO_SERVICE);

        item.addReaderGroup(groupFinal);

        //CREATE
        item = apiSchema.create(ItemTypeA.class, item, users[1].getToken());
        assertNotNull(item);
        assertTrue(item.getReaderGroups().stream()
                .anyMatch(g -> g.getName().equals(groupFinal.getName())));

        //FIND BY NAME
        DbGroup group = apiGroup.findByName(groupFinal.getName(), users[1].getToken());
        assertNotNull(group);

        //UPDATE
        group = apiGroup.update(group.getId(),
                Map.of("userAccessPolicy", DbUserAccessPolicy.MANAGE_BY_ALL.toString()),
                users[1].getToken()
        );
        assertNotNull(group);
        assertEquals(DbUserAccessPolicy.MANAGE_BY_ALL, group.getUserAccessPolicy());

        //TODO: ADD MEMBER
        group = apiGroup.addMember(group.getId(), users[2].getUserId(), users[1].getToken());
        assertNotNull(group);
        assertTrue(group.getMembers().contains(users[2].getUserId()));

        //TODO: REMOVE MEMBER
//        apiGroup.setJwtToken(users[2].getToken());
        group=apiGroup.removeMember(group.getId(), users[2].getUserId(), users[2].getToken());
        assertNotNull(group);
        assertFalse(group.getMembers().contains(users[2].getUserId()));

        HttpStatusCodeException exception = assertThrows(HttpStatusCodeException.class,
                () -> apiGroup.findByName(groupFinal.getName()), users[2].getToken());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        //TODO: FIND ALL
//        apiGroup.setJwtToken(users[1].getToken());
        List<DbGroup> results = apiGroup.findAll(users[1].getToken());
        assertNotNull(results);
        assertEquals(1, results.size());

        //DELETE ITEM
        apiSchema.deleteById(ItemTypeA.class, item.getId(), users[1].getToken());

        //TODO: FIND ORPHANS
        results = apiGroup.getOrphans(users[1].getToken());
        assertNotNull(results);
        assertEquals(1, results.size());

        //DELETE GROUP
        apiGroup.delete(group.getId(),  users[1].getToken());

        exception = assertThrows(HttpStatusCodeException.class,
                () -> apiGroup.findByName(groupFinal.getName(),  users[1].getToken()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        log.info("SCHEMA_TEST::groups | Test finished successfully.");
    }
}
