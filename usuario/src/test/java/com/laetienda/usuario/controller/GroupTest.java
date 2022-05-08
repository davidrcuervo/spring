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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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
             fail();
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
    public void testGroupCycle(){
         String address = "http://localhost:" + port + apiurl + "/create.html";
         Group group = new Group();
         group.setName("testgroup");
         ResponseEntity<Group> response = restclient.postForEntity(address, group, Group.class);
         assertEquals(HttpStatus.OK, response.getStatusCode());
         Group result = response.getBody();
         assertNotNull(result);
         assertTrue(result.getOwners().size() > 0);
         assertTrue(result.getMembers().size() > 0);
     }


}
