package com.laetienda.webapp_test.testApi;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.DbItem;
import com.laetienda.model.schema.ItemTypeA;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface TestSchemaApi {
    void dbItem() throws HttpStatusCodeException;

    ItemTypeA createItem(
            String itemName,
            int age,
            String address,
            String groupName,
            DbUserAccessPolicy userAccessPolicy,
            DbServiceAccessPolicy serviceAccessPolicy,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    DbGroup findByName(
            String groupName,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    DbGroup update(
            long groupId,
            Map<String, String> params,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    DbGroup addMember(
            long groupId,
            String userId,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    DbGroup removeMember(
            long groupId,
            String groupName,
            String userId, String userToken,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    List<DbGroup> findAll(
            String token
    ) throws HttpStatusCodeException, AssertionError;

    <T extends DbItem> void deleteItem(
            Class<T> clazz,
            long itemId,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    List<DbGroup> getOrphans(
            String token
    ) throws HttpStatusCodeException, AssertionError;

    void deleteGroup(
            long groupId,
            String groupName,
            String token
    ) throws HttpStatusCodeException, AssertionError;
}
