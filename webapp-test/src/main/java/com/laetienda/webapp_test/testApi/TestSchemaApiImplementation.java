package com.laetienda.webapp_test.testApi;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.DbGroupPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaGroup;
import com.laetienda.utils.service.api.ApiUser;
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
        groupFinal.setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);

        item.addReaderGroup(groupFinal);

        apiGroup.setJwtToken(users[1].getToken());
        apiSchema.setJwtToken(users[1].getToken());

        //CREATE
        item = apiSchema.create(ItemTypeA.class, item).getBody();
        assertNotNull(item);
        assertTrue(item.getReaderGroups().stream()
                .anyMatch(g -> g.getName().equals(groupFinal.getName())));

        //FIND BY NAME
        DbGroup group = apiGroup.findByName(groupFinal.getName());
        assertNotNull(group);

        //UPDATE
        group = apiGroup.update(group.getId(), Map.of("policy", DbGroupPolicy.MANAGE_BY_ALL.toString()));
        assertNotNull(group);
        assertEquals(DbGroupPolicy.MANAGE_BY_ALL, group.getPolicy());

        //TODO: ADD MEMBER
        group = apiGroup.addMember(group.getId(), users[2].getUserId());
        assertNotNull(group);
        assertTrue(group.getMembers().contains(users[2].getUserId()));

        //TODO: REMOVE MEMBER
        apiGroup.setJwtToken(users[2].getToken());
        group=apiGroup.removeMember(group.getId(), users[2].getUserId());
        assertNotNull(group);
        assertFalse(group.getMembers().contains(users[2].getUserId()));

        HttpStatusCodeException exception = assertThrows(HttpStatusCodeException.class,
                () -> apiGroup.findByName(groupFinal.getName()));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());

        //TODO: FIND ALL
        apiGroup.setJwtToken(users[1].getToken());
        List<DbGroup> results = apiGroup.findAll();
        assertNotNull(results);
        assertEquals(1, results.size());

        //DELETE ITEM
        apiSchema.deleteById(ItemTypeA.class, item.getId());

        //TODO: FIND ORPHANS
        results = apiGroup.getOrphans();
        assertNotNull(results);
        assertEquals(1, results.size());

        //DELETE GROUP
        apiGroup.delete(group.getId());

        exception = assertThrows(HttpStatusCodeException.class,
                () -> apiGroup.findByName(groupFinal.getName()));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        log.info("SCHEMA_TEST::groups | Test finished successfully.");
    }
}
