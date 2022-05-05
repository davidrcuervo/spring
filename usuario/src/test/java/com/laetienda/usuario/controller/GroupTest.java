package com.laetienda.usuario.controller;

import com.laetienda.model.user.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GroupTest {

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
        String address = "http://localhost:" + port + apiurl + "/hola.html";
        List<Group> result = restclient.getForEntity(address, List.class).getBody();
        assertNotNull(result);
        assertTrue(result.size() > 0);

    }
}
