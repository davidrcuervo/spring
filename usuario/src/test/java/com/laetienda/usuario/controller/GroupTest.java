package com.laetienda.usuario.controller;

import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@Import(TestRestClientImpl.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupTest {
    final private static Logger log = LoggerFactory.getLogger(GroupTest.class);
    final private static String USER = "http://localhost:{port}/api/v0/user/user.html?username={username}";
    final private static String USER_CREATE = "http://localhost:{port}/api/v0/user/create.html";
    final private static String USER_DELETE = "http://localhost:{port}/api/v0/user/delete.html?username={username}";
    final private static String GROUPS = "http://localhost:{port}/api/v0/group/groups.html";
    final private static String GROUP = "http://localhost:{port}/api/v0/group/group.html?name={groupName}";
    final private static String CREATE = "http://localhost:{port}/api/v0/group/create.html";
    final private static String ADD_MEMBER = "http://localhost:{port}/api/v0/group/addMember.html?user={user}&group={group}";
    final private static String IS_MEMBER = "http://localhost:{port}/api/v0/group/isMember.html?user={user}&group={group}";
    final private static String UPDATE = "http://localhost:{port}/api/v0/group/update.html?name={groupName}";
    final private static String REMOVE_MEMBER = "http://localhost:{port}/api/v0/group/removeMember.html?group={group}&user={user}";
    final private static String ADD_OWNER = "http://localhost:{port}/api/v0/group/addOwner.html?group={group}&user={user}";
    final private static String REMOVE_OWNER = "http://localhost:{port}/api/v0/group/removeOwner.html?group={group}&user={user}";
    final private static String DELETE = "http://localhost:{port}/api/v0/group/delete.html?name={gname}";
    final private static String ADMUSER = "admuser";
    final private static String ADMUSER_PASSWORD = "secret";


    @LocalServerPort
    private int port;
    private final String apiurl = "/api/v0/group";

    @Autowired
    private TestRestClient restClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @BeforeEach
    public void createTestUser(){
        Usuario user = getTestUser();
        Map<String, String> params =  new HashMap<>();
        params.put("username", user.getUsername());

        ResponseEntity<Usuario> response = restClient.send(USER, port, HttpMethod.GET, null, Usuario.class, params, ADMUSER, ADMUSER_PASSWORD);

        if(response.getStatusCode() == HttpStatus.OK){
            assertNotNull(response.getBody());

        }else if(response.getStatusCode() == HttpStatus.NOT_FOUND){
            response = restClient.send(USER_CREATE, port, HttpMethod.POST, user, Usuario.class, params, null, null);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
        }else {
            fail();
        }
    }

    @AfterEach
    public void removeTestUser(){
        Usuario user = getTestUser();
        Map<String, String> params =  new HashMap<>();
        params.put("username", user.getUsername());

        ResponseEntity<Usuario> resp1 = restClient.send(USER, port, HttpMethod.GET, null, Usuario.class, params, user.getUsername(), user.getPassword());
        if(resp1.getStatusCode() == HttpStatus.NOT_FOUND){
            log.trace("User has been removed and it does not need to be removed");

        }else if(resp1.getStatusCode() == HttpStatus.OK && resp1.getBody() != null){
            ResponseEntity<String> resp2 = restClient.send(USER_DELETE, port, HttpMethod.DELETE, null, String.class, params, user.getUsername(), user.getPassword());
            assertEquals(HttpStatus.OK, resp2.getStatusCode());
            assertTrue(Boolean.valueOf(resp2.getBody()));
        }else{
            fail();
        }

    }

    @Test
    public void testFindAllManagerGroups(){
        ResponseEntity<GroupList> response = restClient.send(GROUPS, port, HttpMethod.GET, null, GroupList.class, null, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Group> groups = response.getBody().getGroups();
        assertTrue(groups.size() > 0);
        groups.forEach((name, group) -> {
            assertTrue(group.getOwners().size() > 0);
            assertTrue(group.getMembers().size() > 0);
            log.trace("Group: {}", group.getName());
        });
        assertTrue(groups.containsKey("manager"));
    }

    @Test
    public void testFindAllByManager(){
        ResponseEntity<GroupList> response = restClient.send(GROUPS, port, HttpMethod.GET, null, GroupList.class, null, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Group> groups = response.getBody().getGroups();
        assertTrue(groups.size() > 0);
        assertTrue(groups.containsKey("manager"));
    }

    @Test
    public void testFindAllByTestUser(){
        ResponseEntity<GroupList> response = restClient.send(GROUPS, port, HttpMethod.GET, null, GroupList.class, null, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Map<String, Group> groups = response.getBody().getGroups();
        assertTrue(groups.size() == 0);
        assertFalse(groups.containsKey("manager"));
    }

    @Test
    public void testFindByName(){
        Map<String, String> params = new HashMap<>();
        params.put("groupName", "manager");
        ResponseEntity<Group> response = restClient.send(GROUP, port, HttpMethod.GET, null,Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        Group result = response.getBody();
        assertEquals("manager", result.getName());
        assertTrue(result.getMembers().size() > 0);
        assertTrue(result.getOwners().size() > 0);
        assertTrue(result.getOwners().containsKey(ADMUSER));
    }

    @Test
    public void testFindByNameNotFound(){
        Map<String, String> params = new HashMap<>();
        params.put("groupName", "invalidgroupname");
        ResponseEntity<Group> response = restClient.send(GROUP, port, HttpMethod.GET, null,Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testFindByNameUnauthorized(){
        Map<String, String> params = new HashMap<>();
        params.put("groupName", "manager");
        ResponseEntity<Group> response = restClient.send(GROUP, port, HttpMethod.GET, null,Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

     @Test
    public void testCreateEmptyGroup(){
         Group group = new Group();

         ResponseEntity<Group> response = restClient.send(CREATE, port, HttpMethod.POST, group, Group.class, null, ADMUSER, ADMUSER_PASSWORD);
         assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
     }

     @Test
     public void testCreateInavalidNameGroup(){
         Group group = new Group();
         group.setName("manager");

         ResponseEntity<Group> response = restClient.send(CREATE, port, HttpMethod.POST, group, Group.class, null, ADMUSER, ADMUSER_PASSWORD);
         assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
     }

     @Test
     public void testCreateGroupWithInvalidMember(){
        //TODO
     }

    @Test
    public void testGroupCycle(){
        create("testgroup");
        addMember("testgroup", "lydac");
        update("testgroup", "newtestgroup");
        removeMember("newtestgroup", "lydac");
        addOwner("newtestgroup", "lydac");
        removeOwner("newtestgroup", "lydac");
        delete("newtestgroup");
    }

    private void delete(String gName){
        Map<String, String> params = new HashMap<>();
        params.put("gname", gName);

        assertEquals(HttpStatus.OK, findGroup(gName).getStatusCode());

        ResponseEntity<String> response = restClient.send(DELETE, port, HttpMethod.DELETE, null, String.class, params, getTestUser().getUsername(), getTestUser().getPassword());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.valueOf(response.getBody()));
        assertEquals(HttpStatus.NOT_FOUND, findGroup(gName).getStatusCode());
    }

    private void removeOwner(String gName, String username) {
        Map<String, String> params = new HashMap<>();
        params.put("group", gName);
        params.put("user", username);

        ResponseEntity<Group> response = findGroup(gName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getOwners().containsKey(username));

        response = restClient.send(REMOVE_OWNER, port, HttpMethod.DELETE, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getOwners().containsKey(username));

        response = findGroup(gName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getOwners().containsKey(username));
    }

    private void addOwner(String gName, String username) {
        Map<String, String> params = new HashMap<>();
        params.put("group", gName);
        params.put("user", username);

        ResponseEntity<Group> response = findGroup(gName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getOwners().containsKey(username));

        response = restClient.send(ADD_OWNER, port, HttpMethod.PUT, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getOwners().containsKey(username));

        response = findGroup(gName);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getOwners().containsKey(username));
    }

    private void removeMember(String gname, String username) {

        Map<String, String> params = new HashMap<>();
        params.put("group", gname);
        params.put("user", username);

        ResponseEntity<Group> response = findGroup(gname);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMembers().containsKey(username));
        assertTrue(Boolean.valueOf(isMember(gname, username).getBody()));

        response = restClient.send(REMOVE_MEMBER, port, HttpMethod.DELETE, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getMembers().containsKey(username));
        assertFalse(Boolean.valueOf(isMember(gname, username).getBody()));
    }

    private void create(String gName){
         Group group = new Group();
         group.setName(gName);
        assertEquals(HttpStatus.NOT_FOUND, findGroup(gName).getStatusCode());
         ResponseEntity<Group> response = restClient.send(CREATE, port, HttpMethod.POST, group, Group.class, null, getTestUser().getUsername(), getTestUser().getPassword());
         assertEquals(HttpStatus.OK, response.getStatusCode());
         Group result = response.getBody();
         assertNotNull(result);
         assertTrue(result.getOwners().size() > 0);
         assertTrue(result.getMembers().size() > 0);
         assertTrue(response.getBody().getOwners().containsKey(getTestUser().getUsername()));
         assertTrue(response.getBody().getMembers().containsKey(getTestUser().getUsername()));
    }

    private void update(String oldGName, String newGName){
        Map<String, String> params = new HashMap<>();
        params.put("groupName", newGName);

        ResponseEntity<Group> resp1 = restClient.send(GROUP, port, HttpMethod.GET, null, Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.NOT_FOUND, resp1.getStatusCode());

        params.put("groupName", oldGName);
        resp1 = restClient.send(GROUP, port, HttpMethod.GET, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        Group group = resp1.getBody();
        assertNotNull(group);
        group.setName(newGName);

        resp1 = restClient.send(UPDATE, port, HttpMethod.PUT, group, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        assertNotNull(resp1.getBody());
        assertEquals(newGName, resp1.getBody().getName());

        resp1 = restClient.send(GROUP, port, HttpMethod.GET, null, Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.NOT_FOUND, resp1.getStatusCode());
        assertEquals(HttpStatus.OK, findGroup(newGName).getStatusCode());
        assertNotNull(findGroup(newGName).getBody());
    }

    private void addMember(String gName, String username){
        Map<String, String> params = new HashMap<>();
        params.put("user", username);
        params.put("group", gName);

        ResponseEntity<String> resp1 = restClient.send(IS_MEMBER, port, HttpMethod.GET, null, String.class, params, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        assertFalse(Boolean.valueOf(resp1.getBody()));

        ResponseEntity<Group> resp2 = restClient.send(ADD_MEMBER, port, HttpMethod.PUT, null, Group.class, params, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.OK, resp2.getStatusCode());
        assertNotNull(resp2.getBody());
        assertTrue(resp2.getBody().getMembers().containsKey("lydac"));

        resp1 = restClient.send(IS_MEMBER, port, HttpMethod.GET, null, String.class, params, getTestUser().getUsername(), getTestUser().getPassword());
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        assertTrue(Boolean.valueOf(resp1.getBody()));
    }

    private ResponseEntity<Group> findGroup(String gName){
        Map<String, String> params = new HashMap<>();
        params.put("groupName", gName);
        return restClient.send(GROUP, port, HttpMethod.GET, null, Group.class, params, ADMUSER, ADMUSER_PASSWORD);
    }

    @Test
    public void testIsMember(){
        ResponseEntity<String> response = isMember("manager", ADMUSER);
//        ResponseEntity<String> response = isMember("newtestgroup", "lydac");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.valueOf(response.getBody()));

        response = isMember("manager", getTestUser().getUsername());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Boolean.valueOf(response.getBody()));

        response = isMember("groupname", ADMUSER);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRemoveInvalidGroup(){
        String gname = "manager";
        String address = "http://localhost:{port}/api/v0/group/delete.html?name={gname}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("gname", gname);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Group> entity = new HttpEntity<>(headers);

        assertEquals(HttpStatus.OK, findGroup(gname).getStatusCode());
        ResponseEntity<String> response = testRestTemplate.exchange(address, HttpMethod.DELETE, entity, String.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(Boolean.valueOf(response.getBody()));
        assertEquals(HttpStatus.OK, findGroup(gname).getStatusCode());

        params.replace("gname", "validUserAccounts");
        assertEquals(HttpStatus.OK, findGroup("validUserAccounts").getStatusCode());
        response = testRestTemplate.exchange(address, HttpMethod.DELETE, entity, String.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(Boolean.valueOf(response.getBody()));
        assertEquals(HttpStatus.OK, findGroup("validUserAccounts").getStatusCode());
    }

    @Test
    public void testRemoveInvalidOwner(){
        String gName = "manager";
        String username = ADMUSER;

        Map<String, String> params = new HashMap<>();
        params.put("group", gName);
        params.put("user", username);

        ResponseEntity<Group> response = findGroup(gName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getOwners().containsKey(username));

        response = restClient.send(REMOVE_OWNER, port, HttpMethod.DELETE, null, Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    private ResponseEntity<String> isMember(String gName, String username) throws HttpClientErrorException {
        Map<String, String> params = new HashMap<>();
        params.put("group", gName);
        params.put("user", username);

        return restClient.send(IS_MEMBER, port, HttpMethod.GET, null, String.class, params, ADMUSER, ADMUSER_PASSWORD);
    }

    private Usuario getTestUser(){
        Usuario result = null;
        String username = "junittestuser";
        String password = "secretpassword";
        Map<String, String> params = new HashMap<>();
        params.put("username", username);

        ResponseEntity<Usuario> response = restClient.send(USER, port, HttpMethod.GET, null, Usuario.class, params, ADMUSER, ADMUSER_PASSWORD);

        if(response.getStatusCode() == HttpStatus.OK){
            result = response.getBody();
            result.setPassword2(password);
            result.setPassword(password);
        }else if(response.getStatusCode() == HttpStatus.NOT_FOUND){
            result = new Usuario();
            result.setUsername(username);
            result.setEmail("junittestuser@mail.com");
            result.setFirstname("Junit");
            result.setLastname("Test User");
            result.setPassword2(password);
            result.setPassword(password);
        }else{
            fail();
        }

        return result;
    }

    @Test
    public void testFindAllByMember() {
        //TODO
    }
}
