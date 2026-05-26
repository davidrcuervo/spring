package com.laetienda.schema;

import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(SchemaTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SchemaMoreTests {
    private static TestUserDto[] USERS;

    @Autowired SchemaTestMvcRepository repo;
    @Autowired MockMvc mvc;
    @Autowired ToolBoxService tb;

    @BeforeAll
    static void beforeAll(@Autowired UtilsBox utils) throws HttpStatusCodeException {
        USERS = utils.getTestUsers(3, "schema.MoreTestUser");
    }

    @AfterAll
    static void teardown(@Autowired UtilsBox utils) {
        utils.deleteTestUsers(USERS);
    }

    @Test
    void findAll() throws Exception {
        ItemTypeA item1 = repo.create(
                "findAllCreate1",
                18,
                "101-1000 Sherbrooke West",
                null, null, null,
                null, null, null,
                USERS[1].getToken()
        );

        ItemTypeA item2 = repo.create(
                "findAllCreate2",
                20,
                "202-1000 Sherbrooke West",
                Set.of(USERS[1].getUserId()), //readers
                DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY,
                DbServiceAccessPolicy.SERVICE_READ,
                null, null, null, //editors
                USERS[2].getToken()
        );

        ItemTypeA item3 = repo.create(
                "findAllCreate3",
                30,
                "303-1000 Sherbrooke West",
                Set.of(USERS[2].getUserId()), //readers
                DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY,
                DbServiceAccessPolicy.SERVICE_READ,
                Set.of(USERS[1].getUserId()), //editors
                DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY,
                DbServiceAccessPolicy.SERVICE_READ,
                USERS[3].getToken()
        );

        List<ItemTypeA> result = repo.findAll(null, USERS[1].getToken());
        assertNotNull(result);
        assertEquals(3, result.size());

        result = repo.findAll(
                Map.of("reader", USERS[2].getUserId()),
                USERS[1].getToken()
        );
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getId().equals(item2.getId())));
        assertTrue(result.stream().anyMatch(item -> item.getId().equals(item3.getId())));

        result = repo.findAll(
                Map.of("editor", ""),
                USERS[1].getToken()
        );
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getId().equals(item1.getId())));
        assertTrue(result.stream().anyMatch(item -> item.getId().equals(item3.getId())));

        result = repo.findAll(
                Map.of("editor", USERS[1].getUserId()),
                USERS[2].getToken()
        );
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(item -> item.getId().equals(item3.getId())));

        result = repo.findAll(
                Map.of("editor", USERS[2].getUserId()),
                USERS[3].getToken()
        );
        assertNotNull(result);
        assertTrue(result.isEmpty());

        repo.remove(item1.getId(), USERS[1].getToken());
        repo.remove(item2.getId(), USERS[2].getToken());
        repo.remove(item3.getId(), USERS[3].getToken());
    }

    @Test
    void findAllWithWrongParams() throws Exception {
        String address = tb.setAddressParams(
                Map.of("manager", ""),
                repo.findAllUri,
                repo.clazzName
        );

        mvc.perform(get(address)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
        ).andExpect(status().isBadRequest());
    }

    @Test
    void findReadersAndEditors() throws Exception {
        ItemTypeA item = repo.create(
                "findReadersTestSchema",
                45,
                "203-1453 Jean Talon West",
                Set.of(USERS[1].getUserId(), USERS[2].getUserId()), //readers
                DbUserAccessPolicy.MANAGE_BY_ALL,
                DbServiceAccessPolicy.SERVICE_WRITE,
                Set.of(USERS[1].getUserId()), //editors
                DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY,
                DbServiceAccessPolicy.SERVICE_READ,
                USERS[3].getToken()
        );

        List<String> readers = repo.getReaders(item.getId(), USERS[1].getToken());
        assertNotNull(readers);
        assertEquals(3, readers.size());
        assertTrue(readers.contains(USERS[1].getUserId()));
        assertTrue(readers.contains(USERS[2].getUserId()));
        assertTrue(readers.contains(USERS[3].getUserId()));

        List<String> editors = repo.getEditors(item.getId(), USERS[1].getToken());
        assertNotNull(editors);
        assertEquals(2, editors.size());
        assertTrue(editors.contains(USERS[1].getUserId()));
        assertTrue(editors.contains(USERS[3].getUserId()));
        assertFalse(editors.contains(USERS[2].getUserId()));

        repo.remove(item.getId(), USERS[3].getToken());
    }
}
