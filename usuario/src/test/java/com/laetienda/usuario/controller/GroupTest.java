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

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
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
    public void hola(){
        String address = "http://localhost:" + port + apiurl + "/hola.html";
        assertEquals("Hello Group Word", restclient.getForEntity(address, String.class).getBody());
    }

    @Test
    public void findAll(){
        String address = "http://localhost:" + port + apiurl + "/groups.html";
        GroupList result = restclient.getForEntity(address, GroupList.class).getBody();
        assertNotNull(result);
        assertTrue(result.getGroups().size() > 0);
        result.getGroups().forEach((group) -> {
            assertTrue(group.getOwners().size() > 0);
            assertTrue(group.getMembers().size() > 0);
            log.trace("Group: {}", group.getName());
        });
    }

    @Test
    public void findByName(){
        String address = "http://localhost:" + port + apiurl + "/group.html?name=manager";
        Group result = restclient.getForEntity(address, Group.class).getBody();
        assertNotNull(result);
        log.trace("result: {}", result.toString());
        assertEquals("manager", result.getName());
        assertTrue(result.getMembers().size() > 0);
        assertTrue(result.getOwners().size() > 0);
    }

     @Test
    public void createEmptyGroup(){
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
     public void createInavalidNameGroup(){
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
    public void groupCycle(){
        create("testgroup");
        update("testgroup", "newtestgroup");
        addUser("testgroup", "lydac");
        removeUser("newtestgroup", "lydac");
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

    private void removeOwner(String newtestgroup, String myself) {

    }

    private void addOwner(String newtestgroup, String myself) {

    }

    private void removeUser(String newtestgroup, String lydac) {

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

    private void addUser(String gname, String usernaeme){

    }

    private ResponseEntity<Group> findGroup(String gname){
        String address = "http://localhost:" + port + apiurl + "/group.html?name=" + gname;
        return restclient.getForEntity(address, Group.class);
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
