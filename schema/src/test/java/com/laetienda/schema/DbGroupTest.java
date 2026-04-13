package com.laetienda.schema;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.DbGroupPolicy;
import com.laetienda.model.kc.KcUser;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.model.user.Usuario;
import com.laetienda.utils.service.api.ApiUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SchemaTestConfiguration.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DbGroupTest {
    private static TestUserDto[] USERS;

    private final String clazzName = Base64.getUrlEncoder().encodeToString(ItemTypeA.class.getName().getBytes(StandardCharsets.UTF_8));

    private ItemTypeA[] items;
    private DbGroup[] groups;

    @Autowired private Environment env;
	@Autowired private MockMvc mvc;
	@Autowired private ObjectMapper json;
    @Autowired private ApiUser apiUser;
    @Autowired private RestClient client;
    @Autowired private OAuth2AuthorizedClientManager authorizedClientManager;

    @Value("${api.schema.create.uri}")
    private String createUri;

    @Value("${api.schema.find.uri}")
    private String findTestItemUri;

    @Value("${api.schema.findById.uri}")
    private String findItemByIdUri;

    @Value("${api.schema.update.uri}")
    private String updateTestItemUri;

    @Value("${api.schema.group.uri.findByName}")
    private String findGroupByNameUri;

    @Value("${api.schema.group.uri.delete}")
    private String deleteGroupUri;

    @Value("${api.schema.group.uri.update}")
    private String updateGroupUri;

    @Value("${api.schema.group.uri.member.add}")
    private String addGroupMemberUri;

    @Value("${api.schema.group.uri.member.remove}")
    private String removeGroupMemberUri;

    private void build(int numberOfEntries, String name){

        groups = new DbGroup[numberOfEntries +1];
        items = new ItemTypeA[numberOfEntries +1];

        for(int g = 1; g < groups.length; g++){
            groups[g] = new DbGroup(String.format("testGroup_%d_%s", g, name));
        }

        for(int i=1; i < items.length; i++){
            items[i] = new ItemTypeA(
                    String.format("testItemGroup_%d_%s", i, name), 18+i,
                    String.format("1453 Villeray. Apto %d", i));
        }
    }

    @Test
    void cycle() throws Exception {
        build(2, "cycle");
        groups[1] = createFirstGroup();
        groups[2] = createSecondGroup();
        deleteEntries();
    }

    private DbGroup createFirstGroup() throws Exception {
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);
        items[1].addReaderGroup(groups[1]);

        //SUCCESSFUL: Create group
        MvcResult response = mvc.perform(post(createUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isOk()).andReturn();
        items[1] = json.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);
        assertNotNull(items[1]);
        assertNotNull(items[1].getId());

        getItem(items[1].getUsername(), USERS[1].getToken());

        //Find group by name
        response = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        DbGroup result = json.readValue(response.getResponse().getContentAsString(), DbGroup.class);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(groups[1].getName(), result.getName());
        assertEquals(result.getOwner(), USERS[1].getUserId());

        return result;
    }

    private DbGroup createSecondGroup() throws Exception {
        groups[2].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);

        items[2].addReaderGroup(groups[2]);
        items[2].addReaderGroup(groups[1]);

        //Create second group
        MvcResult response = mvc.perform(post(createUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[2])))
                .andExpect(status().isOk()).andReturn();
        items[2] = json.readValue(response.getResponse().getContentAsString(), ItemTypeA.class);

        //Find group by name
        response = mvc.perform(get(findGroupByNameUri, groups[2].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        DbGroup result = json.readValue(response.getResponse().getContentAsString(), DbGroup.class);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(groups[2].getName(), result.getName());
        assertEquals(result.getOwner(), USERS[2].getUserId());

        getItem(items[2].getUsername(), USERS[1].getToken());

        return result;
    }

    private void deleteEntries() throws Exception {

        for(int i=1; i < groups.length; i++){

            if(groups[i].getId() == null) {
                MvcResult resp = mvc.perform(get(findGroupByNameUri, groups[i].getName())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[i].getToken()))
                        .andExpect(status().isOk()).andReturn();
                groups[i] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);
            }

            mvc.perform(get(findGroupByNameUri, groups[i].getName())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[i].getToken())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            mvc.perform(MockMvcRequestBuilders.delete(deleteGroupUri, groups[i].getId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[i].getToken()))
                    .andExpect(status().isNoContent());

            mvc.perform(get(findGroupByNameUri, groups[i].getName())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[i].getToken())
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        for(int i=1; i < items.length; i++){
            deleteItem(i);
        }
    }

    private void deleteItem(int id) throws Exception {
        String deleteItemUri = env.getProperty("api.schema.deleteById.uri");
        assertNotNull(deleteItemUri);

        Map<String, String> body = Map.of("username", items[id].getUsername());

        mvc.perform(post(findTestItemUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[id].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk());

        mvc.perform(delete(deleteItemUri, items[id].getId(), clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[id].getToken()))
                .andExpect(status().isNoContent());

        mvc.perform(post(findTestItemUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[id].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGroupWithoutPolicy() throws Exception {
        build(1, "createGroupWithoutPolicy");
        groups[1].setName("testDbGroup_createGroupWithoutPolicy");
        items[1].addReaderGroup(groups[1]);

        //BAD_REQUEST: Create group without policy
        mvc.perform(post(createUri,clazzName)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(items[1])))
        .andExpect(status().isBadRequest());
    }

    @Test
    void createWithWrongUserIdList() throws Exception {
        build(1, "createWithWrongUserIdList");

        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL).addMember(USERS[0].userId);
        items[1].addReaderGroup(groups[1]);

        mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isBadRequest());

        groups[1].removeMember(USERS[0].userId);
        groups[1].addMember("not-valid-user-id");

        mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGroupWithServiceAccount() throws Exception {
        build(1,  "createGroupWithServiceAccount");

        mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[0].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createGroupWithOwner() throws Exception{
        build(1,  "createGroupWithOwner");

        items[1].setOwner(USERS[1].getUserId());
        groups[1].setOwner(USERS[1].getUserId());
        items[1].addReaderGroup(groups[1]);

        mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGroupWithRepeatedName() throws Exception {
        String address = env.getProperty("api.schema.group.uri.orphans");
        assertNotNull(address);

        build(2, "createGroupWithRepeatedName");

        groups[1].setName("testDbGroup_createGroupWithRepeatedName").setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);
        groups[2].setName("testDbGroup_createGroupWithRepeatedName").setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);
        items[1].addReaderGroup(groups[1]);
        items[1].addEditorGroup(groups[2]);

        mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isBadRequest());

        MvcResult resp = mvc.perform(get(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        List<DbGroup> orphans = json.readValue(resp.getResponse().getContentAsString(), new TypeReference<List<DbGroup>>(){});
        assertNotNull(orphans);
        assertTrue(orphans.size() == 1);
        assertTrue(orphans.getFirst().getName().equals(groups[1].getName()));

        Map<String, String> body = Map.of("username", items[1].getUsername());
        mvc.perform(post(findTestItemUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        mvc.perform(delete(deleteGroupUri, orphans.getFirst().getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateItemAfterAddingNewGroup() throws Exception {
        build(1, "updateItemAfterAddingGroup");

        MvcResult resp = mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isOk()).andReturn();
        items[1] = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);
        items[1].addReaderGroup(groups[1]);

        mvc.perform(put(updateTestItemUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(items[1])))
                .andExpect(status().isBadRequest());

        deleteItem(1);
    }

    @Test
    void updateItemAfterAddingExistingGroup() throws Exception {
        build(2, "updateItemAfterAddingExistingGroup");

        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);
        items[1].addEditorGroup(groups[1]);

        groups[2].setPolicy(DbGroupPolicy.MANAGE_BY_OWNER_ONLY);
        items[2].addEditorGroup(groups[2]);

        MvcResult resp = mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isOk()).andReturn();
        items[1] = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);

        resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        resp = mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[2])))
                .andExpect(status().isOk()).andReturn();
        items[2] = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);

        items[2].addEditorGroup(groups[1]);

        mvc.perform(put(updateTestItemUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[2])))
                .andExpect(status().isBadRequest());

        deleteEntries();
    }

    @Test
    void deleteGroupUnauthorized() throws Exception {
        build(1, "deleteGroupUnauthorized");

        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        items[1].addEditorGroup(groups[1]);

        MvcResult resp = mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isOk()).andReturn();
        items[1] = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);

        mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        mvc.perform(delete(deleteGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isUnauthorized());

        deleteEntries();
    }

    @Test
    void updateGroupName() throws Exception{
        build(1, "updateGroupName");
        String name = "testGroup_updatedName";

        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        items[1].addEditorGroup(groups[1]);

        Map<String, String> body = Map.of("name", name);

        MvcResult resp = mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[1])))
                .andExpect(status().isOk()).andReturn();
        items[1] = json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);

        resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        resp = mvc.perform(put(updateGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn();

        mvc.perform(get(findGroupByNameUri, groups[1].getName())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());

        resp = mvc.perform(get(findGroupByNameUri, name)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        deleteEntries();
    }

    @Test
    void updateGroupOwner() throws Exception{
        build(1, "updateOwner");
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        items[1].addEditorGroup(groups[1]);
        items[1] = createTestItem(1);

        MvcResult resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        Map<String, String> body = Map.of("owner", USERS[2].getUserId());
        resp = mvc.perform(put(updateGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);
        assertEquals(groups[1].getOwner(), USERS[2].getUserId());

        mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()).andReturn();

        mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        mvc.perform(delete(deleteGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateWithInvalidName() throws Exception{
        build(2,  "updateWithInvalidName");
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        groups[2].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);

        items[1].addEditorGroup(groups[1]);
        items[2].addEditorGroup(groups[2]);
        items[1] = createTestItem(1);
        items[2] = createTestItem(2);

        MvcResult resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        Map<String, String> body = Map.of("name", groups[2].getName());

        mvc.perform(put(updateGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        deleteEntries();
    }

    @Test
    void updateGroupWithBadOwner() throws Exception{
        build(1, "updateGroupWithBadOwner");
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        items[1].addEditorGroup(groups[1]);
        items[1] = createTestItem(1);

        MvcResult resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        Map<String, String> body = Map.of("owner", USERS[0].getUserId());

        mvc.perform(put(updateGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        deleteEntries();
    }

    @Test
    void updateGroupOwnerByNoOwner() throws Exception{
        fail();
    }

    @Test
    void updateWithInvalidPolicy() throws Exception{
        build(1, "updateWithInvalidPolicy");
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        items[1].addEditorGroup(groups[1]);
        items[1] = createTestItem(1);

        MvcResult resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        Map<String, String> body = Map.of("policy", "notValidPolicy");

        mvc.perform(put(updateGroupUri, groups[1].getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        deleteEntries();
    }

    @Test
    void addReaderMember() throws Exception{
        build(1, "readerMember");
        groups[1].setPolicy(DbGroupPolicy.MANAGE_BY_ALL);
        items[1].addReaderGroup(groups[1]);
        items[1] = createTestItem(1);

        MvcResult resp = mvc.perform(get(findGroupByNameUri, groups[1].getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        mvc.perform(get(findItemByIdUri, items[1].getId(), clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isUnauthorized());

        resp = mvc.perform(put(addGroupMemberUri, groups[1].getId(), USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        groups[1] = json.readValue(resp.getResponse().getContentAsString(), DbGroup.class);

        mvc.perform(get(findItemByIdUri, items[1].getId(), clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        deleteEntries();
    }

    @Test
    void editorMember(){
        build(2, "editorMember");
         items[1].addEditorGroup(groups[1]);
        fail();
    }

    @Test
    void addInvalidMember() throws Exception{
        fail();
    }

    @Test
    void addMemberInvalidGroupId() throws Exception{
        fail();
    }

    @Test
    void addMemberByUnauthorizedUser() throws Exception{
        fail();
    }

    @Test
    void policy(){
        fail();
    }

    @Test
    void updatePolicy(){
        fail();
    }

    private ItemTypeA createTestItem(int c) throws Exception {
        MvcResult resp = mvc.perform(post(createUri,clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[c].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(items[c])))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);
    }

    @BeforeAll
    static void setup(
            @Autowired ApiUser apiUser,
            @Autowired Environment env,
            @Autowired OAuth2AuthorizedClientManager authorizedClientManager) throws Exception {

        int numberOfUsers = 2;
        String clientRegistrationId = env.getProperty("kc.client-registration-id.webapp");
        assertNotNull(clientRegistrationId);

        USERS = new TestUserDto[numberOfUsers + 1];

        String userName = "schemaGroup";
        String[] firstName = {"First", "Second", "Third", "Forth",  "Fifth", "Sixth"};

        for(int j = 1; j <= numberOfUsers; j++) {
            String u = String.format("testUser%d_%s", j, userName);
            Usuario user = new Usuario(u,
                    firstName[j], null, userName,
                    u+"@address.com", false,
                    "secreteTestPassword"+j, "secreteTestPassword"+j
            );

            KcUser kcUser = apiUser.create(user, clientRegistrationId);
            apiUser.enable(kcUser.getId(), clientRegistrationId);
            String token = apiUser.getToken(user.getUsername(), user.getPassword());

            USERS[j] = new TestUserDto(kcUser.getId(), token);
        }

        //SET SERVICE ACCOUNT IN USERS ARRAY
        String kcCerts = env.getProperty("api.kc.realm.certs");
        assertNotNull(kcCerts);

        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(clientRegistrationId)
                .principal("test-system") // Identity of the requester
                .build();

        // This triggers the POST to Keycloak if the token is missing or expired
        OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);
        String serviceAccountToken = authorizedClient.getAccessToken().getTokenValue();
        Jwt jwt = NimbusJwtDecoder.withJwkSetUri(kcCerts).build().decode(serviceAccountToken);

        USERS[0] = new TestUserDto(jwt.getSubject(), serviceAccountToken);
    }

    @AfterAll
    static void tearDown(@Autowired ApiUser apiUser) {

        for(int j = 1; j < USERS.length; j++) {
            apiUser.delete(USERS[j].userId, USERS[j].getToken());
        }
    }

    private ItemTypeA getItem(String username, String jwtToken) throws Exception {
        Map<String, String> body = Map.of("username", username);

        MvcResult resp = mvc.perform(post(findTestItemUri, clazzName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn();

        return  json.readValue(resp.getResponse().getContentAsString(), ItemTypeA.class);
    }
}