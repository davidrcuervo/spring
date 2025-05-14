package com.laetienda.webapp_test.module;

import org.junit.jupiter.api.Test;

public interface GroupModule {
    void setPort(int port);
    void testGroupCycle();
    void testChangeNameOfGroup();
    void findAll();
    void testFindAllByManager();
    void testFindByNameNotFound();
    void testFindByNameUnauthorized();
    void testCreateEmptyGroup();
    void testCreateInavalidNameGroup();
    void testCreateGroupWithInvalidMember();
    void testRemoveInvalidGroup();
    void testFindAllByMember();
    void createGroupWithMembersAndOwners();
}
