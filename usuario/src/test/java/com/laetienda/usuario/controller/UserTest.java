package com.laetienda.usuario.controller;

import com.laetienda.lib.model.AuthCredentials;
import com.laetienda.lib.service.TestRestClient;
import com.laetienda.lib.service.TestRestClientImpl;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;

import org.apache.coyote.Response;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Import(TestRestClientImpl.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {
    final private static String ADMUSER = "admuser";
    final private static String ADMUSER_PASSWORD = "secret";
    final private static Logger log = LoggerFactory.getLogger(UserTest.class);
    final private static String AUTHENTICATE = "http://localhost:{port}/api/v0/user/authenticate.html";
    final private static String CREATE = "http://localhost:{port}/api/v0/user/create.html";
    final private static String DELETE = "http://localhost:{port}/api/v0/user/delete.html?username={username}";
    final private static String GROUP_CREATE = "http://localhost:{port}/api/v0/group/create.html";
    final private static String GROUP_DELETE = "http://localhost:{port}/api/v0/group/delete.html?name={gname}";
    final private static String GROUP_ADD_MEMBER = "http://localhost:{port}/api/v0/group/addMember.html?user={username}&group={gname}";
    final private static String GROUP_IS_MEMBER = "http://localhost:{port}/api/v0/group/isMember.html?group={gname}&user={username}";
    final private static String GROUP_FIND_ALL_BY_MEMBER = "http://localhost:{port}/api/v0/group/groups.html?user={username}";
    final private static String GROUP_FIND_BY_NAME = "http://localhost:{port}/api/v0/group/group.html?name={gname}";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestRestClient restClient;

    private final String apiurl = "/api/v0/user";

    private ResponseEntity<Usuario> findByUsername(String username){
        String address = "http://localhost:{port}/api/v0/user/user.html?username={username}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("admuser", "secret"));
        HttpEntity entity = new HttpEntity(headers);

        return restTemplate.exchange(address, HttpMethod.GET, entity, Usuario.class, params);
    }

    @Test
    public void testAuthentication(){

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        AuthCredentials user = new AuthCredentials("admuser", "secret");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthCredentials> entity = new HttpEntity<>(user, headers);

        ResponseEntity<GroupList> response = restTemplate.exchange(AUTHENTICATE, HttpMethod.POST, entity, GroupList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getGroups().size() > 0);
        response.getBody().getGroups().forEach((name, group) -> {
            log.trace("$gname: {}, $description: {}, #ofOwners: {}, #ofMembers");
        });

        //TODO
        //CREATE TEST USER
        //TEST AUTHENTICATION
        //REMOVE TEST USER
    }

    @Test //Should return 404 not found error if invalid user, and return null on invalid password
    public void testAuthenticationWithIvalidUsername(){
        String address = "http://localhost:{port}/api/v0/user/authenticate.html";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        AuthCredentials user = new AuthCredentials("admuser", "incorrectpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthCredentials> entity = new HttpEntity<>(user, headers);

        ResponseEntity<GroupList> response = restTemplate.exchange(address, HttpMethod.POST, entity, GroupList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        user.setUsername("invaliduser");
        entity = new HttpEntity<>(user, headers);
        response = restTemplate.exchange(address,HttpMethod.POST, entity, GroupList.class, params);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testFindAll(){
        String address = "http://localhost:{port}/api/v0/user/users.html";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("admuser", "secret"));

        HttpEntity entity = new HttpEntity<>(headers);

        ResponseEntity<UsuarioList> response = restTemplate.exchange(address, HttpMethod.GET, entity, UsuarioList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getUsers().size() > 0);
    }

    @Test
    public void testFindAllUnautorized(){
        String address = "http://localhost:{port}/api/v0/user/users.html";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("myself", "password"));

        HttpEntity entity = new HttpEntity<>(headers);

        ResponseEntity<UsuarioList> response = restTemplate.exchange(address, HttpMethod.GET, entity, UsuarioList.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testFindByUsername(){
        String address = "http://localhost:{port}/api/v0/user/user.html?username={username}";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", "admuser");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("admuser", "secret"));

        HttpEntity entity = new HttpEntity<>(headers);

        ResponseEntity<Usuario> response = restTemplate.exchange(address, HttpMethod.GET, entity, Usuario.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("admuser", response.getBody().getUsername());
    }

    @Test
    public void testFindByUsernameRoleManager(){
        String address = "http://localhost:{port}/api/v0/user/user.html?username={username}";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", "myself");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("admuser", "secret"));

        HttpEntity entity = new HttpEntity<>(headers);

        ResponseEntity<Usuario> response = restTemplate.exchange(address, HttpMethod.GET, entity, Usuario.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("myself", response.getBody().getUsername());
    }

    @Test
    public void testFindByUsernameUnauthorized(){
        String address = "http://localhost:{port}/api/v0/user/user.html?username={username}";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", "admuser");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("myself", "password"));

        HttpEntity entity = new HttpEntity<>(headers);

        ResponseEntity<Usuario> response = restTemplate.exchange(address, HttpMethod.GET, entity, Usuario.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testFindByUsernameNotFound(){
        String address = "http://localhost:{port}/api/v0/user/user.html?username={username}";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", "invalidusername");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", getEncode64("admuser", "secret"));

        HttpEntity entity = new HttpEntity<>(headers);

        ResponseEntity<Usuario> response = restTemplate.exchange(address, HttpMethod.GET, entity, Usuario.class, params);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUserCycle(){
        testUserCycleCreate("testuser");
        testUserCycleUpdate("testuser");
        testUserCycleDelete("testuser");
    }

    private void testUserCycleDelete(String username) {
        String address = "http://localhost:{port}/api/v0/user/delete.html?username={username}";
        Map<String, String> params = new HashMap<>();
        params.put("username", username);

        ResponseEntity<Usuario> response = findByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ResponseEntity<String> response1 = restClient.send(address, port, HttpMethod.DELETE, null, String.class, params, "testuser", "secretpassword");
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertTrue(Boolean.valueOf(response1.getBody()));

        response = findByUsername(username);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    private void testUserCycleUpdate(String username) {
        String address = "http://localhost:{port}/api/v0/user/update.html";

        ResponseEntity<Usuario> response = findByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        Usuario user = response.getBody();
        assertEquals(username, user.getUsername());
        assertNull(user.getMiddlename());
        assertEquals("Test Surename", user.getFullName());

        user.setMiddlename("Middle");
        response = restClient.send(address, port, HttpMethod.PUT, user, Usuario.class, null, username, "secretpassword");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(username, user.getUsername());
        assertEquals("Middle", response.getBody().getMiddlename());
        assertEquals("Test Middle Surename", response.getBody().getFullName());
    }

    private void testUserCycleCreate(String username) {
        ResponseEntity<Usuario> response = findByUsername(username);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Usuario user = new Usuario();
        user.setPassword("secretpassword");
        user.setPassword2("secretpassword");
        user.setUsername(username);
        user.setEmail("emailaddress@domain.com");
        user.setFirstname("Test");
        user.setLastname("Surename");

        String address = "http://localhost:{port}/api/v0/user/create.html";

        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Usuario> entity = new HttpEntity<>(user, headers);

        response = restTemplate.exchange(address, HttpMethod.POST, entity, Usuario.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());

        response = findByUsername(username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(username, response.getBody().getUsername());

        ResponseEntity<GroupList> response2 = restClient.send(AUTHENTICATE, port, HttpMethod.POST, null, GroupList.class, null, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testCreateUserRepeatedUsername(){
        String address = "http://localhost:{port}/api/v0/user/create.html";

        Usuario user = new Usuario();
        user.setUsername("admuser");
        user.setFirstname("Test");
        user.setLastname("Last");
        user.setEmail("emailaddress@domain.com");
        user.setPassword2("secretpassword");
        user.setPassword("secretpassword");

        ResponseEntity<Usuario> response = findByUsername(user.getUsername());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        response = restClient.send(address, port, HttpMethod.POST, user, Usuario.class, null, null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

    }

    @Test
    public void testCreateUserRepeatedEmail(){
        String address = "http://localhost:{port}/api/v0/user/create.html";
        Usuario user = findByUsername("admuser").getBody();
        String email = user.getEmail();
        user = getUser();
        user.setEmail(email);
        ResponseEntity<Usuario> response = restClient.send(address, port, HttpMethod.POST, user, Usuario.class, null, null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testCreateUserBadPassword(){
        String address = "http://localhost:{port}/api/v0/user/create.html";
        Usuario user = getUser();
        user.setPassword2("differentpassword");
        ResponseEntity<Usuario> response = restClient.send(address, port, HttpMethod.POST, user, Usuario.class, null, null, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testDeleteNotFound(){
        String address = "http://localhost:{port}/api/v0/user/delete.html?username={username}";
        Map<String, String> params = new HashMap<>();
        params.put("username", "novalidusername");

        ResponseEntity<String> response = restClient.send(address, port, HttpMethod.DELETE, null, String.class, params, "admuser", "secret");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteUnauthorized(){
        String create = "http://localhost:{port}/api/v0/user/create.html";
        String delete = "http://localhost:{port}/api/v0/user/delete.html?username={username}";
        String authenticate = "http://localhost:{port}/api/v0/user/authenticate.html";

        Usuario user = getUser();
        Map<String, String> params = new HashMap<>();
        params.put("username", user.getUsername());

        //Create first user
        ResponseEntity<Usuario> resp1 = restClient.send(create, port, HttpMethod.POST, user, Usuario.class, null, null, null);
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        ResponseEntity<GroupList> resp2 = restClient.send(authenticate, port, HttpMethod.POST, user, GroupList.class, null, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, resp2.getStatusCode());

        //Create second user
        resp1 = restClient.send(create, port, HttpMethod.POST, getSecondUser(), Usuario.class, null, null, null);
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        resp2 = restClient.send(authenticate, port, HttpMethod.POST, user, GroupList.class, null, getSecondUser().getUsername(), getSecondUser().getPassword());
        assertEquals(HttpStatus.OK, resp2.getStatusCode());

        //Try to delete unauthorized
        ResponseEntity<String> resp3 = restClient.send(delete, port, HttpMethod.DELETE, null, String.class, params ,getSecondUser().getUsername(), getSecondUser().getPassword());
        assertEquals(HttpStatus.UNAUTHORIZED, resp3.getStatusCode());

        //Delete first user
        resp3 = restClient.send(delete, port, HttpMethod.DELETE, null, String.class, params ,user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, resp3.getStatusCode());
        assertTrue(Boolean.valueOf(resp3.getBody()));

        //Delete second user
        params.put("username", getSecondUser().getUsername());
        resp3 = restClient.send(delete, port, HttpMethod.DELETE, null, String.class, params ,getSecondUser().getUsername(), getSecondUser().getPassword());
        assertEquals(HttpStatus.OK, resp3.getStatusCode());
        assertTrue(Boolean.valueOf(resp3.getBody()));
    }

    @Test
    public void testDeleteAdmin(){
        String delete = "http://localhost:{port}/api/v0/user/delete.html?username={username}";

        Usuario user = getUser();
        Map<String, String> params = new HashMap<>();
        params.put("username", "admuser");

        ResponseEntity<String> response = restClient.send(delete, port, HttpMethod.DELETE, null, String.class, params, "admuser", "secret");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testDeleteUser(){
        Map<String, String> params = new HashMap<>();

        //ADD USER
        Usuario user = getUser();
        params.put("username", user.getUsername());
        ResponseEntity<Usuario> resp1 = restClient.send(CREATE, port, HttpMethod.POST, user, Usuario.class, null, null, null);
        assertEquals(HttpStatus.OK, resp1.getStatusCode());
        assertNotNull(resp1.getBody());

        //CREATE GROUP AS OWNER
        Group userGroup = new Group();
        userGroup.setName("testDeleteLastOwnerOfGroup");
        params.put("gname", userGroup.getName());
        ResponseEntity<Group> resp2 = restClient.send(GROUP_CREATE, port, HttpMethod.POST, userGroup, Group.class, null, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, resp2.getStatusCode());
        assertNotNull(resp2.getBody());
        assertTrue(resp2.getBody().getOwners().containsKey(user.getUsername()));

        //DELETE USER
        ResponseEntity<String> resp3 = restClient.send(DELETE, port, HttpMethod.DELETE, null, String.class, params, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.FORBIDDEN, resp3.getStatusCode());

        //DELETE GROUP
        resp3 = restClient.send(GROUP_DELETE, port, HttpMethod.DELETE, null, String.class, params, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, resp3.getStatusCode());
        assertTrue(Boolean.valueOf(resp3.getBody()));

        //CREATE GROUP AS MANAGER
        Group managerGroup = new Group();
        managerGroup.setName("testRemoveMemberGroup");
        params.put("gname", managerGroup.getName());
        resp2 = restClient.send(GROUP_CREATE, port, HttpMethod.POST, managerGroup, Group.class, null, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, resp2.getStatusCode());
        assertNotNull(resp2.getBody());

        //ADD MEMBER TO GROUP
        resp2 = restClient.send(GROUP_ADD_MEMBER, port, HttpMethod.PUT, null, Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, resp2.getStatusCode());
        assertNotNull(resp2.getBody());
        assertTrue(resp2.getBody().getMembers().containsKey(user.getUsername()));

        //TEST USER IS MEMBER OF THE GROUP
        resp3 = restClient.send(GROUP_IS_MEMBER, port, HttpMethod.GET, null, String.class, params, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, resp3.getStatusCode());
        assertTrue(Boolean.valueOf(resp3.getBody()));

        //DELETE USER
        resp3 = restClient.send(DELETE, port, HttpMethod.DELETE, null, String.class, params, user.getUsername(), user.getPassword());
        assertEquals(HttpStatus.OK, resp3.getStatusCode());
        assertTrue(Boolean.valueOf(resp3.getBody()));
        resp1 = findByUsername(user.getUsername());
        assertEquals(HttpStatus.NOT_FOUND, resp1.getStatusCode());

        //TEST USER SHOULD NOT BE MEMBER OF ANY GROUP
        ResponseEntity<GroupList> resp4 = restClient.send(GROUP_FIND_ALL_BY_MEMBER, port, HttpMethod.GET, null, GroupList.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, resp4.getStatusCode());
        assertNotNull(resp4.getBody());
        assertFalse(resp4.getBody().getGroups().size() > 0);

        //REMOVE GROUP AS MANAGER
        resp3 = restClient.send(GROUP_DELETE, port, HttpMethod.DELETE, null, String.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.OK, resp3.getStatusCode());
        assertTrue(Boolean.valueOf(resp3.getBody()));
        resp2 = restClient.send(GROUP_FIND_BY_NAME, port, HttpMethod.GET, null, Group.class, params, ADMUSER, ADMUSER_PASSWORD);
        assertEquals(HttpStatus.NOT_FOUND, resp2.getStatusCode());
    }

    private String getEncode64(String username, String password){
        String creds = String.format("%s:%s", username, password);
        String result = new String(Base64.encodeBase64String(creds.getBytes()));
        return "Basic " + result;
    }

//    private <T> ResponseEntity<T> send(String apiurl, HttpMethod httpMethod, Object data, Class<T> clazz, Map<String, String> params, String username, String password) {
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        if(username != null && password != null) {
//            headers.add("Authorization", getEncode64(username, password));
//        }
//
//        HttpEntity<?> entity;
//
//        if(data == null){
//            entity = new HttpEntity(headers);
//        }else{
//            entity = new HttpEntity<Object>(data, headers);
//        }
//
//        if(params == null){
//            params = new HashMap<>();
//        }
//        params.put("port", String.valueOf(port));
//
//        return restclient.exchange(apiurl, httpMethod, entity, clazz, params);
//    }

    private Usuario getUser(){
        Usuario user = new Usuario();
        user.setUsername("testuser");
        user.setFirstname("Test");
        user.setLastname("Last");
        user.setEmail("emailaddress@domain.com");
        user.setPassword2("secretpassword");
        user.setPassword("secretpassword");
        return user;
    }

    private Usuario getSecondUser(){
        Usuario user = new Usuario();
        user.setUsername("anothertestuser");
        user.setFirstname("Test");
        user.setLastname("Last");
        user.setEmail("address@domain.com");
        user.setPassword2("secretpassword");
        user.setPassword("secretpassword");
        return user;
    }
}
