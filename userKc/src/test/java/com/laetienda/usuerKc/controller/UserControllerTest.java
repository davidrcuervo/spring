package com.laetienda.usuerKc.controller;

import com.laetienda.lib.service.KeycloakGrantedAuthoritiesConverter;
import com.laetienda.usuerKc.configuration.UserKcSecurity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import({UserKcSecurity.class, KeycloakGrantedAuthoritiesConverter.class})
class UserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private Environment env;

    @Test
    void unrestricted() throws Exception {
        String address = env.getProperty("api.usuario.test.path"); //api/v0/user/test.html
        Assertions.assertNotNull(address);
        mvc.perform(get(address))
                .andExpect(status().isOk());
    }

    @Test
    void authentication() throws Exception {
        String address = env.getProperty("api.usuario.login.path"); //api/v0/user/login.html
        assertNotNull(address);
        mvc.perform(get(address))
                .andExpect(status().isOk());
    }

    @Test
    void authorization() throws Exception {
        String address = env.getProperty("api.usuario.testAuthorization.path"); //api/v0/user/testAuthorization.html
        assertNotNull(address);
        mvc.perform(get(address))
                .andExpect(status().isOk());
    }
}