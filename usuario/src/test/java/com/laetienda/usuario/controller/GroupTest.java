package com.laetienda.usuario.controller;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupTest {
    final private static Logger log = LoggerFactory.getLogger(GroupTest.class);

    @LocalServerPort
    private int port;
    private final String apiurl = "/api/v0/group";


    @Autowired
    private TestRestTemplate restclient;

    @Test
    public void testHola(){
        String address = "http://localhost:" + port + apiurl + "/hola.html";
        assertEquals("Hello Group Word", restclient.getForEntity(address, String.class).getBody());
    }

    @Test
    public void testFindAll(){
        String address = "http://localhost:" + port + apiurl + "/groups.html";
        GroupList result = restclient.getForEntity(address, GroupList.class).getBody();
        assertNotNull(result);
        assertTrue(result.getGroups().size() > 0);
        result.getGroups().forEach((name, group) -> {
            assertTrue(group.getOwners().size() > 0);
            assertTrue(group.getMembers().size() > 0);
            log.trace("Group: {}", group.getName());
        });
    }

    @Test
    public void testFindByName(){
        String address = "http://localhost:" + port + apiurl + "/group.html?name=manager";
        Group result = restclient.getForEntity(address, Group.class).getBody();
        assertNotNull(result);
        log.trace("result: {}", result.toString());
        assertEquals("manager", result.getName());
        assertTrue(result.getMembers().size() > 0);
        assertTrue(result.getOwners().size() > 0);
    }

     @Test
    public void testCreateEmptyGroup(){
         String address = "http://localhost:" + port + apiurl + "/create.html";
         Group group = new Group();

         try{
             ResponseEntity<Group> response = restclient.postForEntity(address, group, Group.class);
             assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

         }catch(HttpClientErrorException e){
             assertNotNull(e);
             assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
             log.trace("error body: {}", e.getResponseBodyAsString());
         }catch(Exception e){
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
             fail();
         }
     }

     @Test
     public void testCreateInavalidNameGroup(){
         String address = "http://localhost:" + port + apiurl + "/create.html";
         Group group = new Group();
         group.setName("manager");

         try{
             ResponseEntity<Group> response = restclient.postForEntity(address, group, Group.class);
             log.trace("Response Code: {}", response.getStatusCode());
             assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

         }catch(HttpClientErrorException e){
             assertNotNull(e);
             assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
             log.trace("error response: {}", e.getResponseBodyAsString());
         }catch(Exception e){
             log.error(e.getMessage());
             log.debug(e.getMessage(), e);
             fail();
         }
     }

    @Test
    public void testGroupCycle(){
        create("testgroup");
        addMember("testgroup", "lydac");
        update("testgroup", "newtestgroup");
        removeMember("newtestgroup", "lydac");
        addOwner("newtestgroup", "myself");
        removeOwner("newtestgroup", "myself");
        delete("newtestgroup");
    }

    private void delete(String gname){
        String address = "http://localhost:{port}/api/v0/group/delete.html?name={gname}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("gname", gname);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Group> entity = new HttpEntity<>(headers);

        assertEquals(HttpStatus.OK, findGroup(gname).getStatusCode());
        ResponseEntity<Boolean> response = restclient.exchange(address, HttpMethod.DELETE, entity, Boolean.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND, findGroup(gname).getStatusCode());
    }

    private void removeOwner(String gname, String username) {
        String address = "http://localhost:{port}/api/v0/group/removeOwner.html?group={group}&user={user}";
        Map<String, String> params = new HashMap<>();
        params.put("group", gname);
        params.put("user", username);
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<Group> response = findGroup(gname);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getOwners().containsKey(username));

        response = restclient.exchange(address, HttpMethod.DELETE, entity, Group.class, params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getOwners().containsKey(username));

        response = findGroup(gname);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getOwners().containsKey(username));
    }

    private void addOwner(String gname, String username) {
        String address = "http://localhost:{port}/api/v0/group/addOwner.html?group={group}&user={user}";
        Map<String, String> params = new HashMap<>();
        params.put("group", gname);
        params.put("user", username);
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<Group> response = findGroup(gname);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().getOwners().containsKey(username));

        response = restclient.exchange(address, HttpMethod.PUT, entity, Group.class, params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getOwners().containsKey(username));

        response = findGroup(gname);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getOwners().containsKey(username));
    }

    private void removeMember(String gname, String username) {
        String address = "http://localhost:{port}/api/v0/group/removeMember.html?group={group}&user={user}";
        Map<String, String> params = new HashMap<>();
        params.put("group", gname);
        params.put("user", username);
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<Group> response = findGroup(gname);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMembers().containsKey(username));
        assertTrue(Boolean.valueOf(isMember(gname, username).getBody()));

        response = restclient.exchange(address, HttpMethod.DELETE, entity, Group.class, params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getMembers().containsKey(username));
        assertFalse(Boolean.valueOf(isMember(gname, username).getBody()));
    }

    private void create(String gname){
         String address = "http://localhost:" + port + apiurl + "/create.html";
         Group group = new Group();
         group.setName(gname);
        assertEquals(HttpStatus.NOT_FOUND, findGroup(gname).getStatusCode());
         ResponseEntity<Group> response = restclient.postForEntity(address, group, Group.class);
         assertEquals(HttpStatus.OK, response.getStatusCode());
         Group result = response.getBody();
         assertNotNull(result);
         assertTrue(result.getOwners().size() > 0);
         assertTrue(result.getMembers().size() > 0);
    }

    private void update(String oldgname, String newgname){
//        String address = "http://localhost:" + port + apiurl + "/update.html";
        String address = "http://localhost:{port}/api/v0/group/update.html?name={name}";
        ResponseEntity<Group> response;
        Map<String, String> params = new HashMap<>();
        Group group;

        params.put("port", Integer.toString(port));
        params.put("name", oldgname);
        assertEquals(HttpStatus.NOT_FOUND, findGroup(newgname).getStatusCode());
        response = findGroup(oldgname);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        group = response.getBody();
        assertNotNull(group);
        group.setName(newgname);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Group> entity = new HttpEntity<>(group, headers);
        response = restclient.exchange(address, HttpMethod.PUT, entity, Group.class, params);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newgname, response.getBody().getName());
        assertEquals(HttpStatus.NOT_FOUND, findGroup(oldgname).getStatusCode());
        assertEquals(HttpStatus.OK, findGroup(newgname).getStatusCode());
        assertNotNull(findGroup(newgname).getBody());
    }

    private void addMember(String gname, String username){
        String address = "http://localhost:{port}/api/v0/group/addMember.html?user={user}&group={group}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("user", username);
        params.put("group", gname);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<String> response = isMember(gname, username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Boolean.valueOf(isMember(gname, username).getBody()));

        ResponseEntity<Group> gresponse = restclient.exchange(address, HttpMethod.PUT, entity, Group.class, params);
        assertEquals(HttpStatus.OK, gresponse.getStatusCode());
        assertNotNull(gresponse.getBody());
        assertTrue(gresponse.getBody().getMembers().containsKey("lydac"));

        response = isMember(gname, username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.valueOf(response.getBody()));
    }

    private ResponseEntity<Group> findGroup(String gname){
        String address = "http://localhost:" + port + apiurl + "/group.html?name=" + gname;
        return restclient.getForEntity(address, Group.class);
    }

    @Test
    public void testIsMember(){
        ResponseEntity<String> response = isMember("manager", "admuser");
//        ResponseEntity<String> response = isMember("newtestgroup", "lydac");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Boolean.valueOf(response.getBody()));

        response = isMember("manager", "myself");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(Boolean.valueOf(response.getBody()));

        response = isMember("groupname", "admuser");
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
        ResponseEntity<String> response = restclient.exchange(address, HttpMethod.DELETE, entity, String.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(Boolean.valueOf(response.getBody()));
        assertEquals(HttpStatus.OK, findGroup(gname).getStatusCode());

        params.replace("gname", "validUserAccounts");
        assertEquals(HttpStatus.OK, findGroup("validUserAccounts").getStatusCode());
        response = restclient.exchange(address, HttpMethod.DELETE, entity, String.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(Boolean.valueOf(response.getBody()));
        assertEquals(HttpStatus.OK, findGroup("validUserAccounts").getStatusCode());
    }

    @Test
    public void testRemoveInvalidOwner(){
        String gname = "manager";
        String username = "admuser";
        String address = "http://localhost:{port}/api/v0/group/removeOwner.html?group={group}&user={user}";
        Map<String, String> params = new HashMap<>();
        params.put("group", gname);
        params.put("user", username);
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<Group> response = findGroup(gname);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getOwners().containsKey(username));

        response = restclient.exchange(address, HttpMethod.DELETE, entity, Group.class, params);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    private ResponseEntity<String> isMember(String gname, String username) throws HttpClientErrorException {
        String address = "http://localhost:{port}/api/v0/group/isMember.html?user={user}&group={gname}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("gname", gname);
        params.put("user", username);

        return restclient.getForEntity(address, String.class, params);
    }

    @Test
    public void testParams(){
        String address = "http://localhost:{port}/api/v0/group/params.html?name={name}";
//        String addr =
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("name", "test");
        ResponseEntity<String> response = restclient.getForEntity(address, String.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
