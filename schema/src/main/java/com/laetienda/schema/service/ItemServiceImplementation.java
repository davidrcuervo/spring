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
import java.util.Map;

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
    public <T> T create(Class<T> clazz, String data) throws NotValidCustomException {
        try {
            log.debug("ITEM_SERVICE::create $clazzName: {}", clazz.getName());
            String username = request.getUserPrincipal().getName();

            //Build object
            DbItem item = (DbItem) jsonMapper.readValue(data, clazz);

            //Check if object is valid
            readersAndEditorsExists(item);
            item.setOwner(username);

            //Persist
            schemaRepo.create(clazz, item);
            log.trace("SCHEMA_REPO::create $item.id: {}", item.getId());

            //convert to json string
            return ((T) item);

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

    @Override
    public <T> T find(Class<T> clazz, Map<String, String> body) throws NotValidCustomException {
        log.debug("ITEM_SERVICE::find $clazzName: {}", clazz.getName());
        String username = request.getUserPrincipal().getName();

        if(body.size() == 1) {
            T item = schemaRepo.find(clazz, body);

            if (item == null) {
                String message = String.format("Item does not exist.");
                throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "item");
            } else if (canRead((DbItem) item)) {
                return item;
            } else {
                String message = String.format("User, %s, doesn't have privileges to read the item.");
                throw  new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
            }
        }else{
            String message = String.format("Request body has more paramenters than expected");
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
        }
    }

    @Override
    public <T> void delete(Class<T> clazz, Map<String, String> body) throws NotValidCustomException {
        T item = find(clazz, body);
        Long id = ((DbItem)item).getId();

        log.debug("ITEM_SERVICE::delete $clazzName: {}", clazz.getName());
        String username = request.getUserPrincipal().getName();

        if(canEdit((DbItem)item)){
            schemaRepo.delete(clazz, item);
            log.trace("ITEM_SERVICE::delete. $item.id: {}", id);
        }else{
            String message = String.format("User, %s, doesn't have privileges to remove the item. $item.id: %d, $username: %s", ((DbItem) item).getId(), username);
            throw  new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
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
        return username.equals(item.getOwner()) || item.getEditors().contains(username);
    }

    private Boolean canRead(DbItem item){
        String username = request.getUserPrincipal().getName();
        return username.equals(item.getOwner()) || canEdit(item) || item.getReaders().contains(username);
    }
}
