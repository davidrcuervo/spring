package com.laetienda.schema.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.schema.DbItem;
import com.laetienda.schema.repository.ItemRepository;
import com.laetienda.schema.repository.SchemaRepository;
import com.laetienda.utils.lib.Attention;
import com.laetienda.utils.service.api.ApiUser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;

@Service
public class ItemServiceImplementation implements ItemService{
    private final static Logger log = LoggerFactory.getLogger(ItemServiceImplementation.class);

    private final ItemRepository itemRepo;
    private final SchemaRepository schemaRepo;
    private final DbGroupService groupService;
    private final HttpServletRequest request;
    private final ApiUser apiUser;
    private final ObjectMapper jsonMapper;
    private final ToolBoxService tb;

    ItemServiceImplementation(
            ItemRepository itemRepository,
            SchemaRepository schemaRepository,
            DbGroupService dbGroupService,
            HttpServletRequest httpServletRequest,
            ApiUser apiUser,
            ObjectMapper objectMapper,
            ToolBoxService toolBoxService
            ){

        this.itemRepo = itemRepository;
        this.schemaRepo = schemaRepository;
        this.groupService = dbGroupService;
        this.request = httpServletRequest;
        this.apiUser = apiUser;
        this.jsonMapper = objectMapper;
        this.tb = toolBoxService;

    }

    @Value("${webapp.user.service.userId}")
    private String serviceUserId;
//    @Value("${admuser.username}")
//    private String admUser;
//
//    @Value("${admuser.password}")
//    private String admUserPassword;
//
//    @Value("${backend.username}")
//    private String backendUsername;
//
//    @Value("${backend.password}")
//    private String backendPassword;

    @Override
    public <T> T create(Class<T> clazz, String data) throws NotValidCustomException {
        try {
            log.debug("ITEM_SERVICE::create $clazzName: {}", clazz.getName());
            log.trace("ITEM_SERVICE::create. $data: {}", data);

            //Build object
            DbItem item = (DbItem) jsonMapper.readValue(data, clazz);

            //check if owner is valid
            setOwner(item);

            //Check if object is valid
            readersAndEditorsExists(item);

            //Process groups of item
            groupService.create(item);

            //Persist
            schemaRepo.create(clazz, item);
            log.trace("SCHEMA_SERVICE::create $item.id: {}", item.getId());

            //convert to json string
            return clazz.cast(item);
//            return ((T) item);

        }catch (JsonProcessingException ex1){
            log.error("SCHEMA_SERVICE::create $error: {}", ex1.getMessage());
            log.trace(ex1.getMessage(), ex1);
            throw new NotValidCustomException(ex1.getMessage(), HttpStatus.BAD_REQUEST, "item");
//        }catch (Exception ex){
//            log.error("SCHEMA_REPO::create $error: {}", ex.getMessage());
//            log.trace(ex.getMessage(), ex);
//            throw new NotValidCustomException(ex.getMessage(), HttpStatus.BAD_REQUEST, "item");
        }
    }

    private void setOwner(DbItem item) throws NotValidCustomException {
        String userId = apiUser.getCurrentUserId();
        log.trace("ITEM_SERVICE::setOwner. $loggedUser: {}", userId);

        if(item.getOwner() != null && !item.getOwner().equals(userId)){
            String message = String.format("Item owner is different to logged user. $item.owner: %s | $currentUser: %s", item.getOwner(), userId);
            log.warn("ITEM_SERVICE::setOwner. {}", message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
        }

        item.setOwner(userId);
    }

    @Override
    public <T> T find(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException{
        log.debug("ITEM_SERVICE::find $clazzName: {}", clazz.getName());

        if(body.size() == 1) {
            T item = schemaRepo.find(clazz, body);
            return find(clazz, item);
        }else{
            String message = "Request body has more parameters than expected";
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @Override
    public <T> T findById(Class<T> clazz, Long id) throws HttpStatusCodeException {
        log.debug("ITEM_SERVICE::findById $clazzName: {}, $id: {}", clazz.getName(), id);
        T item = schemaRepo.findById(id, clazz);
        return find(clazz, item);
    }

    @Override
    public List<? extends DbItem> findAll(
            Class<? extends DbItem> clazz,
            Map<String, String> params
    ) throws HttpStatusCodeException {
        String userId = request.getUserPrincipal().getName();
        log.debug("ITEM_SERVICE::findAll $clazzName: {} | $userId: {}", clazz.getName(), userId);

        params.forEach((key, value) -> {
            if(!List.of("editor", "reader", "owner", "clazzNameEncoded").contains(key)){
                log.warn("SERVICE_ITEM::findAll | {}", Attention.INVALID_PARAM.getError(key));
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, Attention.INVALID_PARAM.getMessage(key));
            }
        });

        return schemaRepo.findAll(clazz, params, userId).stream()
                .filter(item -> canRead(item, userId))
                .toList();
    }

    private <T> T find(Class<T> clazz, T item) throws HttpStatusCodeException{
        String userId = request.getUserPrincipal().getName();
        if (item == null) {
            String message = "Item does not exist.";
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, message);
        } else if (canRead((DbItem) item)) {
            return item;
        } else {
            String message = String.format("User, %s, doesn't have privileges to read the item.", userId);
            log.info(message);
            throw  new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);
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
        if(item == null){
            String message = String.format("Failed to delete item. Item does not exist. $clazzName: %s, %d: {}", clazz.getName(), id);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "item");
        }

        delete(clazz, item);
    }

    private <T> void delete(Class<T> clazz, T item) throws NotValidCustomException{
        String username = request.getUserPrincipal().getName();
        Long id = ((DbItem)item).getId();

        if(!canEdit((DbItem)item)) {
            String message = String.format("User, %s, doesn't have privileges to remove the item. $item.id: %d", username, ((DbItem) item).getId());
            throw  new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
        }

        try {
            schemaRepo.delete(clazz, item);
            log.trace("ITEM_SERVICE::delete. $item.id: {}", id);
        }catch(Exception ex){
            log.error("ITEM_SERVICE::delete. {} -> {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
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
//              check if editors a readers exist
                readersAndEditorsExists(newItem);

                //test if owner is modified, if so, check that principal is old owner
                if(!newItem.getOwner().equals(oldItem.getOwner()) && !oldItem.getOwner().equals(request.getUserPrincipal().getName())){
                    String message = String.format("%s can't modify the owner of item with id %d", request.getUserPrincipal().getName(), oldItem.getId());
                    throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
                }else{
                    try{
                        apiUser.isUserIdValid(newItem.getOwner());
                    }catch(HttpClientErrorException e){
                        String message = String.format("New owner, %s, is not valid user.", newItem.getOwner());
                        log.warn("ITEM_SERVICE::update. {}", message);
                        throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST);
                    }
                }

                //Check for item groups
                groupService.updateItem(newItem, oldItem);

                T update = schemaRepo.update(clazz, newItem);
                return clazz.cast(newItem);
            }else{
                String message = String.format("%s can't edit the item with id. $id: %d", request.getUserPrincipal().getName(), newItem.getId());
                throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
            }
        } catch (JsonProcessingException ex1) {
            log.error("ITEM_SERVICE::update $error: {}", ex1.getMessage());
            log.trace(ex1.getMessage(), ex1);
            throw new NotValidCustomException(ex1.getMessage(), HttpStatus.BAD_REQUEST, "item");
        } catch(DataAccessException d){
            log.warn("ITEM_SERVICE::update.. {}", d.getMessage());
            log.debug("ITEM_SERVICE::update. {}", d.getMessage(), d);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, d.getMessage());
        }
    }

    @Override
    public void deleteUserById(String userId) throws NotValidCustomException {
        log.debug("ITEM_SERVICE::deleteUserById. $userId: {}", userId);
        String loggedUserId = request.getUserPrincipal().getName();

        if(!groupService.findAll().isEmpty()){
            String m = String.format("User can't be removed because it belongs to groups. $userId: %s", userId);
            log.warn("ITEM_SERVICE::deleteUserById. {}", m);
            throw new NotValidCustomException(m, HttpStatus.FORBIDDEN, "user");
        }

        if(loggedUserId.equals(userId) || tb.hasAuthority("role_manager")){

            List<DbItem> itemsByOwner = itemRepo.findByOwner(userId);
            if(itemsByOwner.isEmpty()){

                //It is allowed to remove user from editors and readers
                schemaRepo.deleteUserById(userId);

            }else{
                String message = String.format("User, %s, is owner of database items and can't be removed", userId);
                log.warn("ITEM_SERVICE::deleteUserById. {}", message);
                throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "Item");
            }

        }else{
            String message = String.format("User, %s, does not have authorization to remove user, %s", loggedUserId, userId);
            log.warn("ITEM_SERVICE::deleteUserById.. {}", message);
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "item");
        }
    }

    @Override
    public Long isItemValid(String itemId, String clazzName) throws NotValidCustomException {
        log.debug("SCHEMA_SERVICE::isItemValid. $id: {} | clazzName: {}", itemId, clazzName);

        try{
            Class<? extends DbItem> clazz = Class.forName(clazzName).asSubclass(DbItem.class);
            Long id = Long.parseLong(itemId);
            DbItem item = schemaRepo.findById(id, clazz);

            if(item == null){
                String message = String.format("SCHEMA_SERVICE::isItemValid. Item not found. $id: %d | $clazz: %s", id, clazz.getName());
                log.warn(message);
                throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "item");
            }else{
                return item.getId();
            }

        }catch(ClassNotFoundException c) {
            String message = String.format("SCHEMA_SERVICE::isItemValid. $error: %s", c.getMessage());
            log.error(message);
            log.trace(message, c);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");

        }catch(NumberFormatException n){
            String message = String.format("SCHEMA_SERVICE::isItemValid. Failed to parse id. $itemId: %s | $error: %s", itemId, n.getMessage());
            log.warn(message);
            log.trace(message, n);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
        }
    }

    @Override
    public <T> List<T> findByQuery(Class<T> clazz, Map<String, String> body) throws NotValidCustomException {
        log.debug("SCHEMA_SERVICE::findByQuery. $clazz: {}", clazz.getName());
        body.forEach((String key, String value) ->
            log.trace("SCHEMA_SERVICE::findByQuery. ${} -> {}", key, value)
        );

        if(!body.containsKey("query")){
            String message = "Parameters does not contain query";
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
        }

        List<T> result = schemaRepo.findByQuery(clazz, body);
        return result.stream().filter(item -> {
            try {
                return canRead((DbItem) item);
            } catch (Exception e) {
                return false;
            }
        }).toList();
    }

    @Override
    public List<String> getReaders(
            Class<? extends DbItem> clazz,
            long itemId
    ) throws HttpStatusCodeException {
        log.debug("SERVICE_SCHEMA::getReaders | $itemId {}", itemId);

        DbItem item = findById(clazz, itemId);

        Set<String> editors = itemRepo.getEditors(item.getId());
        Set<String> onlyReaders = itemRepo.getReaders(itemId);

        Set<String> result = new LinkedHashSet<>();
        result.addAll(onlyReaders);
        result.addAll(editors);

        return result.stream().toList();
    }

    @Override
    public List<String> getEditors(
            Class<? extends DbItem> clazz,
            long itemId
    ) throws HttpStatusCodeException {
        log.debug("SERVICE_SCHEMA::getEditors | $itemId {}", itemId);
        DbItem item = findById(clazz, itemId);
        return itemRepo.getEditors(item.getId()).stream().toList();
    }

    private void readersAndEditorsExists(DbItem item) throws NotValidCustomException {

        try {

            if(item.getEditors() != null) {
                for(String editor : item.getEditors()){
                    apiUser.isUserIdValid(editor);
                }
            }

            if(item.getReaders() != null) {
                for(String reader : item.getReaders()){
                    apiUser.isUserIdValid(reader);
                }
            }

        }catch(HttpClientErrorException ex){
            if(ex.getStatusCode().is4xxClientError()){
                String message = "ITEM_SERVICE::verifyReadersAndEditors. Owner, reader or editor does not exist.";
                log.info(message);
                throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "item");
            }

            log.debug("ITEM_SERVICE::verifyReadersAndEditors $code: {}, $error: {}", ex.getStatusCode(), ex.getMessage());
            throw new NotValidCustomException(ex.getMessage(), ex.getStatusCode(), "item");
        }
    }

    private boolean canEdit(DbItem item){
        String userId = request.getUserPrincipal().getName();
        return canEdit(item, userId);
    }

    private boolean canEdit(DbItem item, String userId){
        if(userId.equals(item.getOwner()))
            return true;

        if(item.getEditors().contains(userId))
            return true;

        return item.getEditorGroups().stream().anyMatch(editorGroup -> {
            if(editorGroup.getOwner().equals(userId))
                return true;
            return editorGroup.getMembers().contains(userId);
        });
    }

    private boolean canRead(DbItem item){
        String userId = request.getUserPrincipal().getName();
        return canRead(item, userId);
    }

    private boolean canRead(DbItem item, String userId){
        if(userId.equals(serviceUserId))
            return true;

        if(userId.equals(item.getOwner()))
            return true;

        if(item.getReaders().contains(userId))
            return true;

        if(item.getReaderGroups().stream().anyMatch(
                readerGroup -> {
                    log.trace("ITEM_SERVICE::canRead. $readerGroupOwner: {}", readerGroup.getOwner());
                    if(readerGroup.getOwner().equals(userId))
                        return true;
                    return readerGroup.getMembers().contains(userId);
                })
        ) {
            return true;
        }

        return canEdit(item, userId);
    }
}
