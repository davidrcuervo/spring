package com.laetienda.webapp_test.testApi;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.DbItem;
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
    public ItemTypeA createItem(String itemName, int age, String address, String groupName, DbUserAccessPolicy userAccessPolicy, DbServiceAccessPolicy serviceAccessPolicy, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::createItem");

        ItemTypeA item = new ItemTypeA(itemName, age, address);

        DbGroup groupFinal = new DbGroup(groupName);
        groupFinal.setUserAccessPolicy(userAccessPolicy);
        groupFinal.setServiceAccessPolicy(serviceAccessPolicy);

        item.addReaderGroup(groupFinal);

        //CREATE
        ItemTypeA result = apiSchema.create(ItemTypeA.class, item, token);
        assertNotNull(result);
        assertTrue(result.getReaderGroups().stream()
                .anyMatch(g -> g.getName().equals(groupFinal.getName())));

        return result;
    }

    @Override
    public DbGroup findByName(String groupName, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::findByName | $groupName: {}", groupName);

        DbGroup result = apiGroup.findByName(groupName, token);
        assertNotNull(result);
        return result;
    }

    @Override
    public DbGroup update(long groupId, Map<String, String> params, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::update | $groupId: {}", groupId);

        DbGroup result = apiGroup.update(groupId, params, token);

        assertNotNull(result);
        assertEquals(DbUserAccessPolicy.MANAGE_BY_ALL, result.getUserAccessPolicy());
        return result;
    }

    @Override
    public DbGroup addMember(long groupId, String userId, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::addMember | $groupId: {}, $userId: {}", groupId, userId);

        DbGroup result = apiGroup.addMember(groupId, userId, token);
        assertNotNull(result);
        assertTrue(result.getMembers().contains(userId));
        return result;
    }

    @Override
    public DbGroup removeMember(
            long groupId, String groupName,
            String userId, String userToken,
            String token
    ) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::removeMember | $groupId: {} | $userId: {}",  groupId, userId);

        DbGroup result=apiGroup.removeMember(groupId, userId, token);
        assertNotNull(result);
        assertFalse(result.getMembers().contains(userId));

        HttpStatusCodeException exception = assertThrows(HttpStatusCodeException.class,
                () -> apiGroup.findByName(groupName, userToken));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        return result;
    }

    @Override
    public List<DbGroup> findAll(String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::findAll");

        List<DbGroup> result = apiGroup.findAll(token);
        assertNotNull(result);
        assertEquals(1, result.size());
        return result;
    }

    @Override
    public <T extends DbItem> void deleteItem(Class<T> clazz, long itemId, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::deleteItem | $itemId: {}", itemId);
        apiSchema.deleteById(clazz, itemId, token);
    }

    @Override
    public List<DbGroup> getOrphans(String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::getOrphans");

        List<DbGroup> result = apiGroup.getOrphans(token);
        assertNotNull(result);
        assertEquals(1, result.size());

        return result;
    }

    @Override
    public void deleteGroup(long groupId, String groupName, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_SCHEMA::deleteGroup | $groupId: {}", groupId);

        apiGroup.delete(groupId, token);

        HttpStatusCodeException exception = assertThrows(
                HttpStatusCodeException.class,
                () -> apiGroup.findByName(groupName, token)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
