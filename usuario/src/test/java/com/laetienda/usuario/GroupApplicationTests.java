package com.laetienda.usuario;

import com.laetienda.webapp_test.module.GroupModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import({UsuarioTestConfiguration.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupApplicationTests {

    @LocalServerPort private int port;
    @Autowired private GroupModule groupTest;

    @BeforeEach
    void setPort(){
        groupTest.setPort(port);
    }

    @Test
    void testGroupCycle(){
        groupTest.testGroupCycle();
    }

    @Test
    void testChangeNameOfGroup(){
        groupTest.testChangeNameOfGroup();
    }

    @Test
    void findAll(){
        groupTest.findAll();
    }

    @Test
    void testFindAllByManager(){
        groupTest.testFindAllByManager();
    }

    @Test
    void testFindByNameNotFound(){
        groupTest.testFindByNameNotFound();
    }

    @Test
    void testFindByNameUnauthorized(){
        groupTest.testFindByNameUnauthorized();
    }

    @Test
    void testCreateEmptyGroup(){
        groupTest.testCreateEmptyGroup();
    }

    @Test
    void testCreateInavalidNameGroup(){
        groupTest.testCreateInavalidNameGroup();
    }

    @Test
    void testCreateGroupWithInvalidMember(){
        groupTest.testCreateGroupWithInvalidMember();
    }

    @Test
    void testRemoveInvalidGroup(){
        groupTest.testRemoveInvalidGroup();
    }

    @Test
    void testFindAllByMember(){
        groupTest.testFindAllByMember();
    }

    @Test
    void createGroupWithMembersAndOwners(){
        groupTest.createGroupWithMembersAndOwners();
    }
}