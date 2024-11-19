package com.laetienda.webapp_test.module;

import com.laetienda.model.schema.DbItem;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.model.user.Usuario;
import com.laetienda.webapp_test.service.UserTestService;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.laetienda.webapp_test.service.SchemaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaModuleImplementation implements SchemaModule {
    private final static Logger log = LoggerFactory.getLogger(SchemaModuleImplementation.class);

    @Autowired private SchemaTest schemaTest;
    @Autowired private UserTestService userTest;

    @Value("${admuser.username}")
    private String admuser;

    @Value("${admuser.password}")
    private String admuserPassword;

    @BeforeEach
    void setSchemaTest(){

    }

    @Override
    public void setPort(int port){
        schemaTest.setPort(port);
    }

    @Override
    public void cycle(){
        ItemTypeA item = new ItemTypeA();
        item.setAddress("1453 Villeray");
        item.setAge(43);
        item.setUsername("myself");

        schemaTest.startSession(admuser, admuserPassword);
        item = create(item);
        find(item);
        update(item);
        delete(item);
        schemaTest.endSession();
    }

    @Override
    public void login() {
        schemaTest.startSession(admuser, admuserPassword);
        schemaTest.endSession();
    }

    @Override
    public void createBadEditor() {
        schemaTest.startSession(admuser, admuserPassword);

        ItemTypeA item = new ItemTypeA("createBadEditor", 22, "7775 Des Erables");
        item.addReader("nonExistUser");

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            schemaTest.create(ItemTypeA.class, item);
        });

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        schemaTest.endSession();
    }

    @Override
    public void addReader() {
        ItemTypeA item = new ItemTypeA("schemaAddReader", 22, "Calle 70B # 87B - 24");
        Usuario user = new Usuario(
                "schemaAddReader",
                "Add","Reader","Schema Test",
                "schemaAddReader@mail.com",
                "secretpassword","secretpassword"
        );
        user = userTest.create(user).getBody();
        userTest.emailValidation(user.getEncToken(),user.getUsername(), user.getPassword());

        //create item
        schemaTest.startSession(admuser, admuserPassword);
        item = schemaTest.create(ItemTypeA.class, item).getBody();
        final long itemId = item.getId();
        schemaTest.endSession();

        //find item should fail. user is not reader of the item
        schemaTest.startSession(user.getUsername(), user.getPassword());
        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            schemaTest.findById(ItemTypeA.class, itemId);
        });
        schemaTest.endSession();

        //add reader to item
        schemaTest.startSession(admuser, admuserPassword);
        item.addReader(user.getUsername());
        schemaTest.update(ItemTypeA.class, item);
        schemaTest.endSession();

        //find item. this time it should work.
        schemaTest.startSession(user.getUsername(), user.getPassword());
        schemaTest.findById(ItemTypeA.class, itemId);
        schemaTest.endSession();

        //delete user and item
        schemaTest.startSession(admuser, admuserPassword);
        schemaTest.deleteById(ItemTypeA.class, item.getId());
        schemaTest.endSession();
        userTest.delete(user.getUsername(), user.getUsername(), user.getPassword());
    }

    private ItemTypeA create(ItemTypeA item){
        ResponseEntity<ItemTypeA> resp = schemaTest.create(ItemTypeA.class, item);
        ItemTypeA itemResp = resp.getBody();
        assertTrue(itemResp.getId() > 0);
        assertEquals(item.getAge(), itemResp.getAge());
        assertEquals(item.getUsername(), itemResp.getUsername());
        assertEquals(item.getAddress(), itemResp.getAddress());

        return itemResp;
    }

	private void find(ItemTypeA item) {
		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());
		ResponseEntity<ItemTypeA> resp = schemaTest.find(ItemTypeA.class, body);
		assertEquals(item.getId(), resp.getBody().getId());
	}

    private void update(ItemTypeA item){
        assertNotNull(item.getId());
        assertTrue(item.getId() > 0);
        item.setAddress("5 Place Ville Marie");
        item.setAge(44);

        ResponseEntity<ItemTypeA> resp = schemaTest.update(ItemTypeA.class, item);

        Map<String, String> params = new HashMap<String, String>();
        params.put("username", item.getUsername());
        ItemTypeA itemResp = schemaTest.find(ItemTypeA.class, params).getBody();

        assertEquals("5 Place Ville Marie", itemResp.getAddress());
        assertEquals(44, itemResp.getAge());
    }

	private void delete(ItemTypeA item){
		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());
		schemaTest.delete(ItemTypeA.class, body);
		schemaTest.notFound(ItemTypeA.class, body);
	}
}
