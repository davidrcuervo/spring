package com.laetienda.kcUser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.kc.KcUser;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.model.user.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class UserTestMvcRepository {

    @Value("${api.kcUser.find.uri}")
    protected String findUri;

    @Value("${api.kcUser.find.byUserId.uri}")
    protected String findUserByIdUri;

    @Value("${api.kcUser.uri.create}")
    protected String createUri;

    @Value("${api.kcUser.uri.enable}")
    protected String enableUri;

    @Value("${api.kcUser.token.uri}")
    protected String tokenUri;

    @Value("${api.kcUser.uri.delete}")
    private String deleteUri;

    private final MockMvc mvc;
    private final ObjectMapper json;

    public UserTestMvcRepository(
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        this.mvc = mockMvc;
        this.json = objectMapper;
    }

    public KcUser create(
        String username,
        String firstName,
        String middleName,
        String lastName,
        String email,
        String password,
        String password2,
        String token
    )throws Exception {
        Usuario user = new Usuario(username, firstName, middleName, lastName, email, false, password, password2);
        MvcResult resp = mvc.perform(post(createUri)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(user))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), KcUser.class);
    }

    public KcUser getUserById(String userId, String token) throws Exception{
        MvcResult resp = mvc.perform(get(findUserByIdUri, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), KcUser.class);
    }

    public KcUser enable(String userId, String token)throws Exception {
        MvcResult resp = mvc.perform(put(enableUri, userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isCreated())
            .andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), KcUser.class);
    }

    public String getToken(String username, String password)throws Exception {
        MultiValueMap<String, String> creeds = new LinkedMultiValueMap<>();
        creeds.add("username", username);
        creeds.add("password", password);

        MvcResult resp = mvc.perform(post(tokenUri)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(creeds))
                .andExpect(status().isOk())
                .andReturn();

        return resp.getResponse().getContentAsString();
    }

    public KcUser getUser(String token)throws Exception {
        MvcResult resp = mvc.perform(get(findUri)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), KcUser.class);
    }

    public void remove(String userId, String token)throws Exception {
        mvc.perform(delete(deleteUri, userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
