package com.laetienda.webapp_test.module;

import com.laetienda.model.schema.ItemTypeA;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.laetienda.webapp_test.service.SchemaTest;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaModuleImplementation implements SchemaModule {
    private final static Logger log = LoggerFactory.getLogger(SchemaModuleImplementation.class);

    @Autowired private SchemaTest schemaTest;

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
        delete(item);
        schemaTest.endSession();
    }

    @Override
    public void login() {
        schemaTest.startSession(admuser, admuserPassword);
        schemaTest.endSession();
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

	private void delete(ItemTypeA item){
		Map<String, String> body = new HashMap<String, String>();
		body.put("username", item.getUsername());
		schemaTest.delete(ItemTypeA.class, body);
		schemaTest.notFound(ItemTypeA.class, body);
	}
}
