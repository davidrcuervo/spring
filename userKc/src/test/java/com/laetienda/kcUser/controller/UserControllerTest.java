package com.laetienda.kcUser.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
//@Import(RestClientConfiguration.class)
class UserControllerTest {
    private final static Logger log = LoggerFactory.getLogger(UserControllerTest.class);

//    private final RestClient client;
    @Autowired private MockMvc mvc;
    @Autowired private Environment env;

//    UserControllerTest(RestClient restClient){
//        this.client = restClient;
//    }

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
        mvc.perform(get(address).with(oidcLogin()))
                .andExpect(status().isOk());
    }

    @Test
    void authorization() throws Exception {
        String address = env.getProperty("api.usuario.testAuthorization.path"); //api/v0/user/testAuthorization.html
        assertNotNull(address);
        mvc.perform(get(address).with(oidcLogin()
                        .authorities(new SimpleGrantedAuthority("role_manager"))))
                .andExpect(status().isOk());
    }

    @Test
    void find() throws Exception {
        String address = env.getProperty("api.kcUser.find.uri", ""); //http://127.0.0.1:$8001/api/v0/user/find
        String token = getToken("myself","secret");
        mvc.perform(get(address)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("David Ricardo"))
                .andExpect(jsonPath("$.username").value("myself"));
    }

    @Test
    void token() throws Exception {
        getToken("myself", "secret");
    }

    String getToken(String username, String password) throws Exception {
        String address = env.getProperty("api.kcUser.token.uri"); //http://127.0.0.1:8081/token
        log.debug("KC_USER_TEST::getToken. $address: {}", address);

        MultiValueMap<String, String> creds = new LinkedMultiValueMap<>();
        creds.add("username",username);
        creds.add("password",password);

        MvcResult result = mvc.perform(post(address)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .params(creds))
                .andDo(response -> {
                    log.trace("KC_USER_TEST::token. $response: {}", response);
                })
                .andExpect(status().isOk())
                .andReturn();

        assertNotNull(result.getResponse().getContentAsString());
        log.trace("token: {}", result.getResponse().getContentAsString());
        return result.getResponse().getContentAsString();
    }
}