package com.laetienda.usuario.controller;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.UsuarioList;
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
        HttpEntity<Group> entity = new HttpEntity<>(headers);

        ResponseEntity<UsuarioList> response = restclient.exchange(address, HttpMethod.GET, entity, UsuarioList.class, params);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getUsers().size() > 2);
    }

}
