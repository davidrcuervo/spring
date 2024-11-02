package com.laetienda.usuario.controller;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.UsuarioTestConfiguration;
import com.laetienda.usuario.service.GroupTestService;
import com.laetienda.usuario.service.UserTestService;
import com.laetienda.utils.service.api.ApiClientService;
import com.laetienda.utils.service.api.GroupApi;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(UsuarioTestConfiguration.class)
public class GroupTest {
    final private static Logger log = LoggerFactory.getLogger(GroupTest.class);

    @LocalServerPort
    private int port;

    @Value("${admuser.username}")
    private String admuser;

    @Value("${admuser.hashed.password}")
    private String admuserHashedPassword;

    @Value("${api.group.create}")
    private String uriCreateGroup;

    @Value("${api.group.addMember}")
    private String uriAddMember;

    private String admuserPassword;

//    @Autowired
//    private ApiClientService client;

    @Autowired
    private GroupApi groupApi;

    @Autowired
    private StringEncryptor jasypte;

    @Autowired
    private UserTestService userTest;

    @Autowired
    private GroupTestService groupTest;

    @BeforeEach
    public void setAdmPassword(){
        admuserPassword = jasypte.decrypt(admuserHashedPassword);
        groupApi.setPort(port);
        groupTest.setPort(port);
        groupTest.setAdmuserPassword(admuserPassword);
        userTest.setPort(port);
        userTest.setAdmuserPassword(admuserPassword);
    }

//    @Test
    public void testGroupCycle(){
        Usuario user = new Usuario(
                "testGroupCycle",
                "Cycle",null,"Test Group",
                "testGroupCycle@mail.com",
                "secretpassword","secretpassword");

        Usuario member = new Usuario(
                "memberOfTestGroupCycle",
                "Member","Cycle","Test Group",
                "memberOfTestGroupCycle@mail.com",
                "secretpassword","secretpassword");

        ResponseEntity<Usuario> response = userTest.create(user);
        userTest.emailValidation(response.getBody().getEncToken(), user.getUsername(), user.getPassword());

        ResponseEntity<Usuario> response2 = userTest.create(member);
        userTest.emailValidation(response2.getBody().getEncToken(), member.getUsername(), member.getPassword());

        Group group = new Group("testGroupCycle", null);

        groupTest.create(group, user.getUsername(), user.getPassword());
        addMember(group.getName(), response2.getBody(), user.getUsername(), user.getPassword());
        addOwner(group.getName(), user.getUsername(), user.getPassword());

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            userTest.delete(user.getUsername());
        });
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        HttpClientErrorException ex2 = assertThrows(HttpClientErrorException.class, () -> {
            groupTest.removeOwner(group.getName(), user.getUsername(), user.getUsername(), user.getPassword());
        });
        assertEquals(HttpStatus.FORBIDDEN, ex2.getStatusCode(), "Can't remove last owner of the group");

        updateDescription(group);
        groupTest.delete(group.getName(), user.getUsername(), user.getPassword());

        userTest.delete(user.getUsername());
        userTest.delete(member.getUsername());
    }

    private void addMember(String gName, Usuario member, String loginUsername, String password){
        log.trace("TEST::addMemeber. $groupname: {}, $username: {}, $loginUsername: {}", gName, member.getUsername(), password);

        groupTest.isNotMember(gName, member.getUsername(), loginUsername, password);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            groupTest.addMember(gName, member.getUsername(), member.getUsername(), password);
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        log.trace("TEST::addMember. $error: {}.", ex.getMessage());

        groupTest.addMember(gName, member.getUsername(), loginUsername, password);
        groupTest.isMember(gName, member.getUsername(), loginUsername, password);
        groupTest.removeMember(gName, member.getUsername(), loginUsername, password);
        groupTest.isNotMember(gName, member.getUsername(), loginUsername, password);
    }

    private void addOwner(String groupname, String loginUsername, String password) {
        Usuario owner = new Usuario(
                "ownerOfTestGroupCycle",
                "Owner","Cycle","Test Group",
                "OwnerOfTestGroupCycle@mail.com",
                "secretpassword","secretpassword");

        ResponseEntity<Usuario> response = userTest.create(owner);
        userTest.emailValidation(response.getBody().getEncToken(), owner.getUsername(), owner.getPassword());

        groupTest.isNotOwner(groupname, response.getBody().getUsername(), loginUsername, password);
        groupTest.addOwner(groupname, response.getBody().getUsername(), loginUsername, password);
        groupTest.isOwner(groupname, response.getBody().getUsername(), loginUsername, password);
        assertTrue(groupTest.findByName(groupname, loginUsername, password).getBody().getOwners().containsKey(owner.getUsername()));

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            groupTest.removeMember(groupname, owner.getUsername(), loginUsername, password);
        });
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        groupTest.removeOwner(groupname, response.getBody().getUsername(), loginUsername, password);
        groupTest.removeMember(groupname, owner.getUsername(), loginUsername, password);
        userTest.delete(owner.getUsername());
    }

    @Test
    void testChangeNameOfGroup(){
        Group group = groupTest.create(new Group("testNameOfGroup", null)).getBody();

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
           groupTest.findByName("testChangeNameOfGroup");
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        group.setName("testChangeNameOfGroup");
        groupTest.update("testNameOfGroup", group);
        groupTest.findByName("testChangeNameOfGroup");

        ex = assertThrows(HttpClientErrorException.class, () -> {
            groupTest.findByName("testNameOfGroup");
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        groupTest.delete("testChangeNameOfGroup");
    }

    @Test
    public void findAll(){
        Usuario findall = new Usuario(
                "testFindAllManagerGroups",
                "Find","All","Group Test",
                "testFindAllManagerGroups@email.com",
                "secretpassword","secretpassword"
        );

        ResponseEntity<Usuario> response = userTest.create(findall);
        userTest.emailValidation(response.getBody().getEncToken(), findall.getUsername(), findall.getPassword());

        Group group = new Group("testFindAll", null);
        groupTest.create(group, findall.getUsername(), findall.getPassword());

        ResponseEntity<GroupList> response2 = groupTest.findAll();
        assertEquals(3, response2.getBody().getGroups().size());
        assertTrue(response2.getBody().getGroups().containsKey(group.getName()));

        response2 = groupTest.findAll(findall.getUsername(), findall.getPassword());
        assertEquals(2, response2.getBody().getGroups().size());
        assertTrue(response2.getBody().getGroups().containsKey(group.getName()));

        response2.getBody().getGroups().forEach((groupname, group2) -> {
            assertTrue(group2.getMembers().containsKey(findall.getUsername()));
            assertTrue(group2.getMembers().size() > 0);
            assertTrue(group2.getOwners().size() > 0);
        });

        groupTest.delete(group.getName());
        userTest.delete(findall.getUsername());
    }

    @Test
    public void testFindAllByManager(){
        Usuario user = new Usuario(
                "testFindAllByManager",
                "Findall","Bymanager","Group Test",
                "testFindAllByManager@mail.com",
                "secretpassword","secretpassword"
        );
        ResponseEntity<Usuario> resp = userTest.create(user);
        userTest.emailValidation(resp.getBody().getEncToken(), user.getUsername(), user.getPassword());

        Group group = new Group("testFindAllByManager", null);
        groupTest.create(group, user.getUsername(), user.getPassword());

        ResponseEntity<GroupList> resp2 = groupTest.findAll();
        Map<String, Group> groups = resp2.getBody().getGroups();
        assertTrue(groups.containsKey(group.getName()));
        assertTrue(groups.containsKey("manager"));
        assertTrue(groups.containsKey("validUserAccounts"));

        groupTest.delete(group.getName());
        userTest.delete(user.getUsername(), user.getUsername(), user.getPassword());
    }

    @Test
    public void testFindByNameNotFound(){
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            groupTest.findByName("testFindByNameNotFound");
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    public void testFindByNameUnauthorized(){
        Usuario user = new Usuario(
                "testFindByNameUnauthorized",
                "Find","Unauthroized","Group Test",
                "testFindByNameUnauthorized@mail.com",
                "secretpassword", "secretpassword"
        );
        ResponseEntity<Usuario> resp1 = userTest.create(user);
        userTest.emailValidation(resp1.getBody().getEncToken(), user.getUsername(), user.getPassword());
        groupTest.findByName("validUserAccounts");

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            groupTest.findByName("manager", user.getUsername(), user.getPassword());
        });

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        userTest.delete(user.getUsername());
    }

     @Test
    public void testCreateEmptyGroup(){
         Group group = new Group();
         HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
             groupTest.create(group);
         });
         assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
     }

     @Test
     public void testCreateInavalidNameGroup(){
         Group group = new Group("manager", null);

         HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
             ((GroupApi)groupApi.setPort(port).setCredentials(admuser, admuserPassword))
                     .create(group);
         });

         assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
     }

     @Test
     public void testCreateGroupWithInvalidMember(){
        Group group = new Group("testCreateGroupWithInvalidMember", null);
        Usuario user = new Usuario(
                "testCreateGroupWithInvalidMember",
                "Invalid","Member","Test Group",
                "testCreateGroupWithInvalidMember@mail.com",
                "secretpassword","secretpassword"
        );
        userTest.create(user);

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
             ((GroupApi)groupApi.setPort(port).setCredentials(user.getUsername(), user.getPassword()))
                     .create(group);
        });
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        userTest.delete(user.getUsername(), user.getUsername(), user.getPassword());

     }

    private void delete(String gName){
        fail();
//        Map<String, String> params = new HashMap<>();
//        params.put("gname", gName);
//
//        assertEquals(HttpStatus.OK, findGroup(gName).getStatusCode());
//
//        ResponseEntity<String> response = restClient.send(DELETE, port, HttpMethod.DELETE, null, String.class, params, getTestUser().getUsername(), getTestUser().getPassword());
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertTrue(Boolean.valueOf(response.getBody()));
//        assertEquals(HttpStatus.NOT_FOUND, findGroup(gName).getStatusCode());
    }

    private void removeOwner(String gName, String username) {
        fail();

//        Map<String, String> params = new HashMap<>();
//        params.put("group", gName);
//        params.put("user", username);
//
//        ResponseEntity<Group> response = findGroup(gName);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertTrue(response.getBody().getOwners().containsKey(username));
//
//        response = restClient.send(REMOVE_OWNER, port, HttpMethod.DELETE, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertFalse(response.getBody().getOwners().containsKey(username));
//
//        response = findGroup(gName);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertFalse(response.getBody().getOwners().containsKey(username));
    }

    private void removeMember(String gname, String username) {
        fail();
//
//        Map<String, String> params = new HashMap<>();
//        params.put("group", gname);
//        params.put("user", username);
//
//        ResponseEntity<Group> response = findGroup(gname);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertTrue(response.getBody().getMembers().containsKey(username));
//        assertTrue(Boolean.valueOf(isMember(gname, username).getBody()));
//
//        response = restClient.send(REMOVE_MEMBER, port, HttpMethod.DELETE, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//        assertFalse(response.getBody().getMembers().containsKey(username));
//        assertFalse(Boolean.valueOf(isMember(gname, username).getBody()));
    }

    private void update(String oldGName, String newGName){
        fail();

    }

    private void updateDescription(Group group){
        Group temp = groupTest.findByName(group.getName()).getBody();
        assertNull(temp.getDescription());

        temp.setDescription("This is a test group");
        temp = groupTest.update(temp.getName(), temp).getBody();
        assertNotNull(temp.getDescription());
    }

    @Test
    public void testRemoveInvalidGroup(){
        HttpClientErrorException ex;

        ex = assertThrows(HttpClientErrorException.class, () -> {
            ((GroupApi)groupApi.setPort(port).setCredentials(admuser, admuserPassword))
                .delete("manager");
        });
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        ex = assertThrows(HttpClientErrorException.class, () -> {
            ((GroupApi)groupApi.setPort(port).setCredentials(admuser, admuserPassword))
                    .delete("validUserAccounts");
        });
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());

        Usuario user = new Usuario(
                "testRemoveInvalidGroup",
                "Remove","InvalidGroup","Group Test",
                "testRemoveInvalidGroup@mail.com",
                "secretpassword","secretpassword"
        );
        ResponseEntity<Usuario> resp1 = userTest.create(user);
        userTest.emailValidation(resp1.getBody().getEncToken(), user.getUsername(), user.getPassword());

        Group group = new Group("testRemoveInvalidGroup", null);
        groupTest.create(group);
        groupTest.addMember(group.getName(),user.getUsername(),admuser,admuserPassword);

        ex = assertThrows(HttpClientErrorException.class, () -> {
            ((GroupApi)groupApi.setPort(port).setCredentials(user.getUsername(), user.getPassword()))
                    .delete("testRemoveInvalidGroup");
        });
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        groupTest.addOwner(group.getName(),user.getUsername(),admuser, admuserPassword);
        groupTest.delete(group.getName(),user.getUsername(),user.getPassword());
        userTest.delete(user.getUsername(),user.getUsername(),user.getPassword());
    }

    private ResponseEntity<String> isMember(String gName, String username) throws HttpClientErrorException {
        fail();
//        Map<String, String> params = new HashMap<>();
//        params.put("group", gName);
//        params.put("user", username);
//
//        return restClient.send(IS_MEMBER, port, HttpMethod.GET, null, String.class, params, ADMUSER, ADMUSER_PASSWORD);
        return null;
    }

    private Usuario getTestUser(){
        fail();
//        Usuario result = null;
//        String username = "junittestuser";
//        String password = "secretpassword";
//        Map<String, String> params = new HashMap<>();
//        params.put("username", username);
//
//        ResponseEntity<Usuario> response = restClient.send(USER, port, HttpMethod.GET, null, Usuario.class, params, ADMUSER, ADMUSER_PASSWORD);
//
//        if(response.getStatusCode() == HttpStatus.OK){
//            result = response.getBody();
//            result.setPassword2(password);
//            result.setPassword(password);
//        }else if(response.getStatusCode() == HttpStatus.NOT_FOUND){
//            result = new Usuario();
//            result.setUsername(username);
//            result.setEmail("junittestuser@mail.com");
//            result.setFirstname("Junit");
//            result.setLastname("Test User");
//            result.setPassword2(password);
//            result.setPassword(password);
//        }else{
//            fail();
//        }

//        return result;
        return null;
    }

    @Test
//    public void simpleTest(){
    public void testFindAllByMember() {
        Usuario user = new Usuario(
                "testFindAllByMember",
                "Test","FindAllByMember","Group Test",
                "testFindAllByMember@mail.com",
                "secretpassword", "secretpassword"
        );

        ResponseEntity<Usuario> resp1 = userTest.create(user);
        userTest.emailValidation(resp1.getBody().getEncToken(), user.getUsername(), user.getPassword());

        ResponseEntity<GroupList> resp2 = ((GroupApi)groupApi.setPort(port).setCredentials(admuser, admuserPassword))
                .findAllByMember(user.getUsername());
        assertEquals(HttpStatus.OK, resp2.getStatusCode());
        assertNotNull(resp2.getBody());
        assertTrue(resp2.getBody().getGroups().size() > 0);
        assertTrue(resp2.getBody().getGroups().containsKey("validUserAccounts"));
        userTest.delete("testFindAllByMember", user.getUsername(), user.getPassword());
    }

    @Test
    public void createGroupWithMembersAndOwners(){
        Usuario member = new Usuario(
                "createGroupWithMembers",
                "Create","WithMemebers","Group Test",
                "createGroupWithMembers@mail.com",
                "secretpassword","secretpassword");
        userTest.create(member);

        Group group = new Group("createGroupWithMembers", null);
        group.addMember(member);
        groupTest.create(group);

        Group result = groupTest.findByName(group.getName()).getBody();
        assertTrue(result.getMembers().containsKey(member.getUsername()));

        groupTest.delete(group.getName());
        userTest.delete(member.getUsername());
    }

    @Test
    public void simpleTest(){

        Usuario member = new Usuario(
                "simpleTest",
                "Simple","Test","Group Test",
                "simpletest@mail.com",
                "secretpassword","secretpassword");
//        userTest.create(member);


        Group gropu = new Group("simpleTest", null);


//        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
//            groupTest.removeOwner("testGroupCycle","testGroupCycle", admuser, admuserPassword);
//        });
//
//        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    public void clean(){
//        userTest.delete("testuser");

        //TEST::testGroupCycle
//        groupTest.delete("testGroupCycle");
//        userTest.delete("memberOfTestGroupCycle");
//        userTest.delete("ownerOfTestGroupCycle");
//        userTest.delete("testGroupCycle");

//        groupTest.delete("testNameOfGroup");
    }
}