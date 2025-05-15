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

    @Value("${backend.username}")
    private String backendUsername;

    @Value("${backend.password}")
    private String backendPassword;

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
//        }catch (Exception ex){
//            log.error("SCHEMA_REPO::create $error: {}", ex.getMessage());
//            log.trace(ex.getMessage(), ex);
//            throw new NotValidCustomException(ex.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    @Override
    public <T> T find(Class<T> clazz, Map<String, String> body) throws NotValidCustomException {
        log.debug("ITEM_SERVICE::find $clazzName: {}", clazz.getName());

        if(body.size() == 1) {
            T item = schemaRepo.find(clazz, body);
            return find(clazz, item);
        }else{
            String message = String.format("Request body has more paramenters than expected");
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
        }
    }

    @Override
    public <T> T findById(Class<T> clazz, Long id) throws NotValidCustomException {
        log.debug("ITEM_SERVICE::findById $clazzName: {}, $id: {}", clazz.getName(), id);
        T item = schemaRepo.findById(id, clazz);
        return find(clazz, item);
    }

    private <T> T find(Class<T> clazz, T item) throws NotValidCustomException{
        String username = request.getUserPrincipal().getName();
        if (item == null) {
            String message = String.format("Item does not exist.");
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "item");
        } else if (canRead((DbItem) item)) {
            return item;
        } else {
            String message = String.format("User, %s, doesn't have privileges to read the item.", username);
            throw  new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
        }
    }

    @Override
    public <T> void delete(Class<T> clazz, Map<String, String> body) throws NotValidCustomException {
        T item = find(clazz, body);
        log.debug("ITEM_SERVICE::delete $clazzName: {}", clazz.getName());
        delete(clazz, item);
    }

    @Override
    public <T> void deleteById(Class<T> clazz, Long id) throws NotValidCustomException {
        log.debug("ITEM_SERVICE::deleteById $clazzName: {}, $id: {}", clazz.getName(), id);
        T item = schemaRepo.findById(id, clazz);
        delete(clazz, item);
    }

    private <T> void delete(Class<T> clazz, T item) throws NotValidCustomException{
        String username = request.getUserPrincipal().getName();
        Long id = ((DbItem)item).getId();

        if(canEdit((DbItem)item)){
            schemaRepo.delete(clazz, item);
            log.trace("ITEM_SERVICE::delete. $item.id: {}", id);
        }else{
            String message = String.format("User, %s, doesn't have privileges to remove the item. $item.id: %d, $username: %s", ((DbItem) item).getId(), username);
            throw  new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
        }
    }

    @Override
    public <T> T update(Class<T> clazz, String data) throws NotValidCustomException {
        log.debug("ITEM_SERVICE::update $clazz: {}", clazz.getName());

        try {
            DbItem newItem = (DbItem)jsonMapper.readValue(data, clazz);
            DbItem oldItem = (DbItem)schemaRepo.findById(newItem.getId(), clazz);

            if(oldItem == null){
                String message = String.format("Item with id, %d, does not exist.", newItem.getId());
                throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
            }

            if(canEdit(oldItem)){
                readersAndEditorsExists(newItem);

                //test if owner is modified, if so, check that principal is old owner
                if(!newItem.getOwner().equals(oldItem.getOwner()) && !oldItem.getOwner().equals(request.getUserPrincipal().getName())){
                    String message = String.format("%s can't modify the owner of item with id %d", request.getUserPrincipal().getName(), oldItem.getId());
                    throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
                }
                schemaRepo.update(clazz, newItem);
                return clazz.cast(newItem);
            }else{
                String message = String.format("%s can't edit the item with id. $id: %d", request.getUserPrincipal().getName(), newItem.getId());
                throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
            }
        } catch (JsonProcessingException ex1) {
            log.error("SCHEMA_REPO::create $error: {}", ex1.getMessage());
            log.trace(ex1.getMessage(), ex1);
            throw new NotValidCustomException(ex1.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    private void readersAndEditorsExists(DbItem item) throws NotValidCustomException {

        try {
            ((UserApi)userApi.setCredentials(backendUsername, backendPassword)).startSession();

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
            log.debug("ITEM_SERVICE::verifyReadersAndEditors $code: {}, $error: {}", ex.getStatusCode(), ex.getMessage());
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
