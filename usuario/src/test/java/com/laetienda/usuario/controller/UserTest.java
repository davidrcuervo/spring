package com.laetienda.usuario.controller;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.apache.coyote.Response;
import org.junit.jupiter.api.Test;
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

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restclient;

    private final String apiurl = "/api/v0/user";

    @Test
    public void testFindAll(){
        String address = "http://localhost:{port}/api/v0/user/users.html";
        Map<String, String> params = new HashMap<>();
        params.put("port", Integer.toString(port));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<UsuarioList> response = restclient.exchange(address, HttpMethod.GET, entity, UsuarioList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getUsers().size() > 2);
    }

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
}
