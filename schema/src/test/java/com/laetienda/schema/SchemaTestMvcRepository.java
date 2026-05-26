package com.laetienda.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.ItemTypeA;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Component
public class SchemaTestMvcRepository {
    protected final String clazzName = Base64.getUrlEncoder().encodeToString(ItemTypeA.class.getName().getBytes(StandardCharsets.UTF_8));

    @Value("${api.schema.create.uri}")
    protected String createUri;

    @Value("${api.schema.findById.uri}")
    protected String findByIdUri;

    @Value("${api.schema.findAll.uri}")
    protected String findAllUri;

    @Value("${api.schema.find.readers.uri}")
    private String findReadersUri;

    @Value("${api.schema.find.editors.uri}")
    private String findEditorsUri;

    @Value("${api.schema.deleteById.uri}")
    protected String deleteByIdUri;

    private final MockMvc mvc;
    private final ObjectMapper json;
    private final ToolBoxService tb;

    SchemaTestMvcRepository(
            MockMvc mockMvc,
            ObjectMapper json,
            ToolBoxService toolBoxService
    ) {
        this.mvc = mockMvc;
        this.json = json;
        this.tb = toolBoxService;
    }

    ItemTypeA create(
            String username, Integer age, String address,
            Set<String> readers,
            DbUserAccessPolicy readerUsersPolicy,
            DbServiceAccessPolicy readerServicePolicy,
            Set<String> editors,
            DbUserAccessPolicy editorUsersPolicy,
            DbServiceAccessPolicy editorServicePolicy,
            String token
    )throws Exception {
        ItemTypeA item = new ItemTypeA(username, age, address);

        if(readers != null && !readers.isEmpty()) {
            DbGroup readerGroup = new DbGroup("testSchemaReader." + username);
            readerGroup.setUserAccessPolicy(readerUsersPolicy);
            readerGroup.setServiceAccessPolicy(readerServicePolicy);
            readers.forEach(readerGroup::addMember);
            item.addReaderGroup(readerGroup);
        }

        if(editors != null &&  !editors.isEmpty()) {
            DbGroup editorGroup = new DbGroup("testSchemaWriter." + username);
            editorGroup.setUserAccessPolicy(editorUsersPolicy);
            editorGroup.setServiceAccessPolicy(editorServicePolicy);
            editors.forEach(editorGroup::addMember);
            item.addEditorGroup(editorGroup);
        }

        MvcResult resp = mvc.perform(post(createUri, clazzName)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(item))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        ItemTypeA result = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getId() > 0);

        return result;
    }

    ItemTypeA find(long id, String token)throws Exception {
        MvcResult resp = mvc.perform(get(findByIdUri, id, clazzName)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        ItemTypeA result = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(id, (long) result.getId());

        return result;
    }

    void remove(long id, String token) throws Exception {
        ItemTypeA result = find(id, token);
        assertNotNull(result);

        mvc.perform(delete(deleteByIdUri, id, clazzName)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

        mvc.perform(get(findByIdUri, id, clazzName)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    public List<ItemTypeA> findAll(
            Map<String, String> params,
            String token
    ) throws Exception {

        String address = tb.setAddressParams(params, findAllUri, clazzName);
        MvcResult resp = mvc.perform(get(address)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), new TypeReference<>() {
        });
    }

    public List<String> getReaders(long itemId, String token) throws Exception{
        String address = tb.setAddressParams(Map.of("clazzNameEncoded", clazzName), findReadersUri, itemId);
        MvcResult resp = mvc.perform(get(address)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), new  TypeReference<>() {});
    }

    public List<String> getEditors(long itemId, String token) throws Exception{
        String address = tb.setAddressParams(Map.of("clazzNameEncoded", clazzName), findEditorsUri, itemId);
        MvcResult resp = mvc.perform(get(address)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), new  TypeReference<>() {});
    }
}
