package com.laetienda.webapp_test.repository;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.DbItem;
import com.laetienda.model.schema.ItemTypeA;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface TestSchemaRepo {
    void dbItem() throws HttpStatusCodeException;

    /**
     *
     * @param itemName item name
     * @param age integer must be greater than 18
     * @param address does not have any impact
     * @param groupName name of reader group
     * @param userAccessPolicy policy for reader group
     * @param serviceAccessPolicy policy for reader group
     * @param token of user who will create the test item
     * @return test item representation of DbItem
     * @throws HttpStatusCodeException Bad request if name of group exists or item name exits
     * @throws AssertionError if null
     */
    ItemTypeA createItem(
            String itemName,
            int age,
            String address,
            String groupName,
            DbUserAccessPolicy userAccessPolicy,
            DbServiceAccessPolicy serviceAccessPolicy,
            String token
    ) throws HttpStatusCodeException, AssertionError;

    /**
     * ItemTypeA is a test representation of DbItem for testing proposes
     * @param name item name
     * @param age must be greater than 18
     * @param address does not have any impact
     * @param readers list of users that can read the item
     * @param readersUserAccessPolicy policy for readers group of item
     * @param readersServiceAccessPolicy policy for readers group of item
     * @param editors list of users that can edit the item
     * @param editorsUserAccessPolicy policy for readers group of item
     * @param editorsServiceAccessPolicy policy for readers group of item
     * @param token this user will be the owner of the test item
     * @return ItemTypeA that is a test representation of DbItem
     * @throws HttpStatusCodeException Bad request if name of group exists or item name exits
     * @throws AssertionError if null
     */
    ItemTypeA createItem(
            String name,
            int age,
            String address,
            List<String> readers,
            DbUserAccessPolicy readersUserAccessPolicy,
            DbServiceAccessPolicy readersServiceAccessPolicy,
            List<String> editors,
            DbUserAccessPolicy editorsUserAccessPolicy,
            DbServiceAccessPolicy editorsServiceAccessPolicy,
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

    List<String> getReaders(Long id, String token) throws HttpStatusCodeException, AssertionError;
    List<String> getEditors(Long id, String token) throws HttpStatusCodeException, AssertionError;
}
