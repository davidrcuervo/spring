package com.laetienda.schema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import com.laetienda.model.schema.ItemTypeA;
import com.laetienda.schema.repository.ItemRepository;
import com.laetienda.schema.repository.SchemaRepository;
import com.laetienda.utils.service.api.UserApi;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.security.Principal;

@Service
public class ItemServiceImplementation implements ItemService{
    private final static Logger log = LoggerFactory.getLogger(ItemServiceImplementation.class);

    @Autowired private ItemRepository itemRepo;
    @Autowired private HttpServletRequest request;
    @Autowired private UserApi userApi;
    @Autowired private ObjectMapper jsonMapper;
    @Autowired private SchemaRepository schemaRepo;

    @Value("${admuser.username}")
    private String admUser;

    @Value("${admuser.password}")
    private String admUserPassword;

    @Override
    public String create(String clazzName, String data) throws NotValidCustomException {
        try {
            log.debug("ITEM_SERVICE::create $clazzName: {}", clazzName);
            String username = request.getUserPrincipal().getName();

            //Build object
            Class<?> clazz = Class.forName(clazzName);
            DbItem item = (DbItem) jsonMapper.readValue(data, clazz);

            //Check if object is valid
            readersAndEditorsExists(item);
            item.setOwner(username);

            //Persist
            schemaRepo.create(clazz, item);
            log.trace("SCHEMA_REPO::create $item.id: {}", item.getId());

            //convert to json string
            return jsonMapper.writeValueAsString(clazz.cast(item));

        }catch (JsonProcessingException ex1){
            log.error("SCHEMA_REPO::create $error: {}", ex1.getMessage());
            log.trace(ex1.getMessage(), ex1);
            throw new NotValidCustomException(ex1.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }catch (Exception ex){
            log.error("SCHEMA_REPO::create $error: {}", ex.getMessage());
            log.trace(ex.getMessage(), ex);
            throw new NotValidCustomException(ex.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    private void readersAndEditorsExists(DbItem item) throws NotValidCustomException {

        try {
            ((UserApi)userApi.setCredentials(admUser, admUserPassword)).startSession();

            if(item.getEditors() != null) {
                item.getEditors().forEach((editor) -> {
                    userApi.findByUsername(editor);
                });
            }

            if(item.getReaders() != null) {
                item.getReaders().forEach((reader) -> {
                    userApi.findByUsername(reader);
                });
            }

        }catch(HttpClientErrorException ex){
            log.debug("ITEM_SERVICE::verifyReadersAndEditors $error: {}", ex.getMessage());
            throw new NotValidCustomException(ex.getMessage(), ex.getStatusCode(), "item");

        }finally{
            userApi.endSession();
        }
    }

    private Boolean canEdit(DbItem item){
        String username = request.getUserPrincipal().getName();
        return item.getEditors().contains(username);
    }

    private Boolean canRead(DbItem item){
        String username = request.getUserPrincipal().getName();
        return item.getReaders().contains(username);
    }
}
