package com.laetienda.usuario.controller;

import com.laetienda.lib.model.AuthCredentials;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;

import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTest {
    final private static Logger log = LoggerFactory.getLogger(UserTest.class);

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restclient;

    private final String apiurl = "/api/v0/user";

    @Test
    public void testDeleteInvalidUser(){
        String address = "http://localhost:{port}/api/v0/user/delete.html?username={username}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", "admuser");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<Usuario> userResponse = findByUsername("admuser");
        assertEquals(HttpStatus.OK, userResponse.getStatusCode());
        assertNotNull(userResponse.getBody());
        ResponseEntity<String> response = restclient.exchange(address, HttpMethod.DELETE, entity, String.class, params);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    private ResponseEntity<Usuario> findByUsername(String username){
        String address = "http://localhost:{port}/api/v0/user/user.html?username={username}";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        params.put("username", username);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        return restclient.exchange(address, HttpMethod.GET, entity, Usuario.class, params);
    }

    @Test
    public void testWrongPassword(){
        String address = "http://localhost:{port}/api/v0/user/create.html";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Usuario user = new Usuario();
        user.setUsername("testusername");
        user.setFirstname("name");
        user.setMiddlename("middle");
        user.setLastname("lastname");
        user.setEmail("testaddress@testdomain.com");
        user.setPassword("12345678");
        user.setPassword2("87654321");
        HttpEntity<Usuario> entity = new HttpEntity<>(user, headers);

        ResponseEntity<Usuario> response = findByUsername(user.getUsername());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        response = restclient.exchange(address, HttpMethod.POST, entity, Usuario.class, params);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testValidAuthentication(){
        String address = "http://localhost:{port}/api/v0/user/authenticate.html";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        AuthCredentials user = new AuthCredentials("admuser", "secret");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthCredentials> entity = new HttpEntity<>(user, headers);

        ResponseEntity<GroupList> response = restclient.exchange(address, HttpMethod.POST, entity, GroupList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getGroups().size() > 0);
        response.getBody().getGroups().forEach((name, group) -> {
            log.trace("$gname: {}, $description: {}, #ofOwners: {}, #ofMembers");
        });
    }

    @Test //Should return 404 not found error if invalid user, and return null on invalid password
    public void testInvalidAuthentication(){
        String address = "http://localhost:{port}/api/v0/user/authenticate.html";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));

        AuthCredentials user = new AuthCredentials("admuser", "incorrectpassword");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthCredentials> entity = new HttpEntity<>(user, headers);

        ResponseEntity<GroupList> response = restclient.exchange(address, HttpMethod.POST, entity, GroupList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());

        user.setUsername("invaliduser");
        entity = new HttpEntity<>(user, headers);
        response = restclient.exchange(address,HttpMethod.POST, entity, GroupList.class, params);
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

        ResponseEntity<UsuarioList> response = restclient.exchange(address, HttpMethod.GET, entity, UsuarioList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getUsers().size() > 0);
    }

    private String getEncode64(String username, String password){
        String creds = String.format("%s:%s", username, password);
        String result = new String(Base64.encodeBase64String(creds.getBytes()));
        return "Basic " + result;
    }
}
