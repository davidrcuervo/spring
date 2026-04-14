package com.laetienda.schema.service;

import com.laetienda.lib.options.DbGroupPolicy;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.schema.DbItem;
import com.laetienda.schema.repository.DbGroupRepository;
import com.laetienda.schema.repository.ItemRepository;
import com.laetienda.utils.service.api.ApiUser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbGroupServiceImplementation implements DbGroupService {
    private final static Logger log = LoggerFactory.getLogger(DbGroupServiceImplementation.class);

    private final DbGroupRepository groupRepo;
    private final ItemRepository itemRepo;
    private final ApiUser apiUser;
    private final Validator validator;

    DbGroupServiceImplementation(
            DbGroupRepository dbGroupRepository,
            ItemRepository itemRepository,
            Validator validator,
            ApiUser apiUser) {
        this.groupRepo = dbGroupRepository;
        this.itemRepo = itemRepository;
        this.validator = validator;
        this.apiUser = apiUser;
    }

    @Override
    public DbGroup findByName(String name) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::findByName. $name: {}", name);

        DbGroup result = groupRepo.findByName(name);
        String uid = apiUser.getCurrentUserId();

        if (result == null) {
            String message = String.format("Group, with that name, does not exist: %s", name);
            log.warn("DbGROUP_SERVICE::findByName. {}", message);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, message);
        }

        if(!canRead(uid,  result)) {
            String message = String.format("User does not have authorization to access group. $groupName: %s | $userId: %s", name, uid);
            log.warn("DbGROUP_SERVICE::findByName. {}", message);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);
        }

        return isValid(result) ? result : null;
    }

    @Override
    public void create(DbItem item) throws HttpStatusCodeException {
        String uid = apiUser.getCurrentUserId();
        processNewItemGroups(item.getReaderGroups(), uid, item, false);
        processNewItemGroups(item.getEditorGroups(), uid, item, true);
    }

    void processNewItemGroups(Set<DbGroup> dbGroups, String userId, DbItem item, boolean isEditorList) throws HttpStatusCodeException {
        if(dbGroups != null){
            dbGroups.forEach(group -> {
                log.trace("DbGROUP_SERVICE::processNewItemGroups. $groupId: {} | $groupName: {} | isEditorList: {}",
                        group.getId(),
                        group.getName(),
                        isEditorList
                );

                if(isEditorList) {
                    if(group.getEditorItems() != null && group.getEditorItems().contains(item)) {
                        log.trace("DbGROUP_SERVICE::processNewItemGroups. Item already exists in editor group");
                    }else{
                        group.addEditorItem(item);
                    }
                } else {
                    if (group.getReaderItems() != null && group.getReaderItems().contains(item)) {
                        log.trace("DbGROUP_SERVICE::processNewItemGroups. Item already exists in reader group");
                    } else {
                        group.addReaderItem(item);
                    }
                }

                if(group.getId() == null){

                    if(groupRepo.findByName(group.getName()) != null){
                        String message = String.format("A group with that name already exist. $name: %s", group.getName());
                        log.warn("DbGROUP_SERVICE::processNewItemGroups. {}", message);
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
                    }

                    if(group.getOwner() == null){
                        group.setOwner(userId);
                    }else{
                        String message = String.format("Owner can't be assigned to any group. $ownerUserId: %s", group.getOwner());
                        log.warn("DbGROUP_SERVICE::processNewItemGroups.. {}", message);
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Invalid owner");
                    }

                    if(isValid(group)) {
                        groupRepo.save(group);
                    }else{
                        log.error("DbGROUP_SERVICE::processNewItemGroups. This message should never happen");
                    }
                }
            });
        }
    }

    @Override
    public boolean isValid(DbGroup dbGroup) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::isValid");

        String message;

        if((message = areValidUsers(dbGroup)) != null){
            log.warn("DbGROUP_SERVICE::isValid. {}", message);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
        }

        Set<ConstraintViolation<DbGroup>> violations = validator.validate(dbGroup);
        if(!violations.isEmpty()){

            violations.forEach(v -> {
                log.trace("DbGROUP_SERVICE::isValid. $groupName: {} | {} | {} | {}",
                        dbGroup.getName(),
                        v.getPropertyPath(),
                        v.getInvalidValue(),
                        v.getMessage());
            });

            ConstraintViolation<DbGroup> violation = violations.iterator().next();
            message = String.format("Item group is not valid: %s | %s | %s",
                    violation.getPropertyPath().toString(),
                    violation.getInvalidValue(),
                    violation.getMessage()
                    );
            log.warn("DbGROUP_SERVICE::isValid. {}", message);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
        }

        return true;
    }

    @Override
    public DbGroup find(String groupId) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::find $groupId: {}", groupId);

        try{
            Long gid = Long.parseLong(groupId);
            DbGroup group = groupRepo.findById(gid).orElse(null);

            if (group == null) {
                String m = String.format("Group with id %s not found", groupId);
                log.warn("DbGROUP_SERVICE::find.. {}", m);
                throw new HttpClientErrorException(HttpStatus.NOT_FOUND, m);
            }

            return group;
        }catch(NumberFormatException e){
            String m = String.format("Group with id, %s, is not a valid format", groupId);
            log.warn("DbGROUP_SERVICE::find. {}", m);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, m);
        }
    }

    @Override
    public DbGroup update(String groupId, Map<String, String> body) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::update. $groupId: {}", groupId);

        Optional<DbGroup> group = groupRepo.findById(parseGroupId(groupId));

        if(group.isEmpty()) {
            String m = String.format("A group with that name does not exist. $groupId: %s", groupId);
            log.warn("DbGROUP_SERVICE::update. {}", m);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, m);
        }

        String uid = apiUser.getCurrentUserId();
        if(!canEdit(uid, group.get())){
            String m = String.format("User is not authorized to edit group. $groupId: %s | $userId: %s", groupId, uid);
            log.warn("DbGROUP_SERVICE::update. {}", m);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, m);
        }

        body.forEach((key, value) -> {
            log.trace("DbGROUP_SERVICE::update. $field: {} | $value: {}", key, value);

            if(key.equals("name")){
                updateName(group.get(), value);

            } else if(key.equals("owner")){
                updateOwner(group.get(), value, uid);

            } else if(key.equals("policy")){
                updatePolicy(group.get(), value);

            } else{
                String m = String.format("Invalid update field: $field: %s", key);
                log.warn("DbGROUP_SERVICE::update. {}", m);
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, m);
            }
        });

        try {
            return groupRepo.save(group.get());
        }catch (DataAccessException d){
            log.error("DbGROUP_SERVICE::update. {}", d.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, d.getMessage());
        }
    }

    @Override
    public void updateItem(DbItem newItem, DbItem oldItem) throws HttpStatusCodeException {

        if (newItem.getEditorGroups() != null){

            if(newItem.getEditorGroups().stream().anyMatch(g -> g.getId() == null))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "New DbGroup is present while update item");

            if(oldItem.getEditorGroups() == null){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Groups in new item and old item are different.");

            }else{
                Set<Long> oldItemEditorGroupIds = oldItem.getEditorGroups().stream().map(DbGroup::getId).collect(Collectors.toSet());
                Set<Long> newItemEditorGroupIds = newItem.getEditorGroups().stream().map(DbGroup::getId).collect(Collectors.toSet());

                if(!newItemEditorGroupIds.equals(oldItemEditorGroupIds))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Groups in new item and old item are different.");
            }
        }

        if (newItem.getReaderGroups()  != null){

            if(newItem.getReaderGroups().stream().anyMatch(g -> g.getId() == null))
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "New DbGroup is present while update item");

            if(oldItem.getReaderGroups() == null){
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Groups in new item and old item are different.");

            }else{
                Set<Long> oldItemReaderGroupIds = oldItem.getReaderGroups().stream().map(DbGroup::getId).collect(Collectors.toSet());
                Set<Long> newItemReaderGroupIds = newItem.getReaderGroups().stream().map(DbGroup::getId).collect(Collectors.toSet());

                if(!newItemReaderGroupIds.equals(oldItemReaderGroupIds))
                    throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Groups in new item and old item are different.");
            }

        }
    }

    @Override
    public void delete(String groupId) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::delete $groupId: {}", groupId);
        String uid = apiUser.getCurrentUserId();

        try {
            Long gid = Long.parseLong(groupId);

            groupRepo.findById(gid).ifPresent(group -> {
                if (canEdit(uid, group)) {

                    group.getEditorItems().forEach(item -> {
                        itemRepo.findById(item.getId()).ifPresent(editorItem -> {
                            editorItem.removeEditorGroup(group);
                            itemRepo.save(item);
                        });
                    });

                    group.getReaderItems().forEach(item -> {
                        itemRepo.findById(item.getId()).ifPresent(readerItem -> {
                            readerItem.removeReaderGroup(group);
                            itemRepo.save(item);
                        });
                    });

                    groupRepo.delete(group);

                } else {
                    String message = String.format("You don't have privileges to edit group. $groupId: %s", groupId);
                    log.warn("DbGROUP_SERVICE::delete. {}", message);
                    throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);
                }
            });

        }catch(NoSuchElementException e){
            log.debug("DbGROUP_SERVICE::delete. $groupId: {} | $message: {}", groupId, e.getMessage());
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, e.getMessage());

        }catch(NumberFormatException e){
            log.debug("DbGROUP_SERVICE::delete.. $groupId: {} | $message: {}", groupId, e.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public DbGroup addMember(String groupId, String userId) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::addMember. $groupId: {} | $userId: {}", groupId, userId);
        return addOrRemoveMember(groupId, userId, true);
    }

    @Override
    public DbGroup removeMember(String groupId, String userId) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::removeMember. $groupId: {} | $userId: {}", groupId, userId);
        return addOrRemoveMember(groupId, userId, false);
    }

    private DbGroup addOrRemoveMember(String groupId, String userId, boolean addMember) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::addOrRemoveMember. $addMember: {} | $groupId: {} | $userId: {}", addMember, groupId, userId);

        String currentUserId = apiUser.getCurrentUserId();
        DbGroup group = find(groupId);

        if(!canEdit(currentUserId, group) && !group.getMembers().contains(userId)) {
            String m = String.format("User is not authorized to edit group. $groupId: %s | $userId: %s", groupId, currentUserId);
            log.warn("DbGROUP_SERVICE::addOrRemoveMember.. {}", m);
            throw new  HttpClientErrorException(HttpStatus.UNAUTHORIZED, m);
        }

        try{
            if(addMember) {
                String temp = apiUser.isUserIdValid(userId);
                group.addMember(userId);
            }else{
                group.removeMember(userId);
            }

            return groupRepo.save(group);

        } catch(HttpClientErrorException e){
            if(e.getStatusCode().is4xxClientError()){
                log.warn("DbGROUP_SERVICE::addOrRemoveMember. {}", e.getMessage());
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
            }else{
                throw e;
            }

        } catch(DataAccessException e){
            log.error("DbGROUP_SERVICE::addOrRemoveMember. {}", e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Set<DbGroup> getOrphans() throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::getOrphans.");
        String uid = apiUser.getCurrentUserId();

        Set<DbGroup> temp = groupRepo.findByReaderItemsIsEmptyAndEditorItemsIsEmpty();

        return temp.stream().filter(g -> canRead(uid, g))
                .collect(Collectors.toSet());
    }

    private String areValidUsers(DbGroup dbGroup) throws HttpStatusCodeException {

        Set<String> memberIds = dbGroup.getMembers();

        if(memberIds != null && !memberIds.isEmpty()) {
            memberIds.forEach(memberId -> {

                try {
                    apiUser.isUserIdValid(memberId);
                } catch (HttpStatusCodeException e) {
                    if (e.getStatusCode().is4xxClientError()) {
                        String result = String.format("User, %s, does not exist or is not valid", memberId);
                        log.warn("DbGROUP_SERVICE::areValidUsers. {}", result);
                        throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, result);
                    }else{
                        throw e;
                    }
                }
            });
        }

        return null;
    }

    private boolean canRead(String uid, DbGroup dbGroup) throws HttpStatusCodeException {

        if(dbGroup.getOwner().equals(uid))
            return true;

        else if(dbGroup.getMembers() != null && dbGroup.getMembers().contains(uid))
            return true;

        else
            return false;
    }

    private boolean canEdit(String userId, DbGroup dbGroup) throws HttpStatusCodeException {

        if(dbGroup.getOwner().equals(userId))
            return true;

        if (dbGroup.getPolicy().equals(DbGroupPolicy.MANAGE_BY_OWNER_ONLY))
            return false;

        else if(dbGroup.getPolicy().equals(DbGroupPolicy.MANAGE_BY_ALL))
            return dbGroup.getMembers().contains(userId);

        else{
            String message = String.format("SEVERE | Group contains an undefined policy. $groupPolicy: %s", dbGroup.getPolicy().toString());
            log.error("DbGROUP_SERVICE::canEdit. {}", message);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }
    }

    private Long parseGroupId(String groupId) throws HttpStatusCodeException {
        try{
            return Long.parseLong(groupId);
        }catch(NumberFormatException e){
            String m = String.format("Invalid group id. $groupId: %s | $message: %s", groupId,  e.getMessage());
            log.warn("DbGROUP_SERVICE::parseGroupId. {}", m);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, m);
        }
    }

    private void updateName(DbGroup dbGroup, String newName) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::updateName. $groupId: {} | $newName: {}", dbGroup.getId(), newName);

        if(groupRepo.findByName(newName) != null){
            String m = String.format("Group with that name already exists. $name: %s", newName);
            log.warn("DbGROUP_SERVICE::updateName. {}", m);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, m);
        }

        Set<ConstraintViolation<DbGroup>> violations = validator.validateValue(DbGroup.class, "name", newName);
        if(!violations.isEmpty()) {
            String m = violations.iterator().next().getMessage();
            log.warn("DbGROUP_SERVICE::updateName. {}", m);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, m);
        }

        dbGroup.setName(newName);
    }

    private void updateOwner(DbGroup dbGroup, String owner, String currentUserId) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::updateOwner. $groupId: {} | $owner: {}", dbGroup.getId(), owner);

        if(!dbGroup.getOwner().equals(currentUserId)){
            String m = String.format("User is not owner, only owner can modify the owner. $owner: %s", owner);
            log.warn("DbGROUP_SERVICE::updateOwner. {}", m);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, m);
        }

        if(!dbGroup.getMembers().contains(dbGroup.getOwner())){
            String m = String.format("Current owner is not member, first add current owner as member of group. $currentOwner: %s",  dbGroup.getOwner());
            log.warn("DbGROUP_SERVICE::updateOwner. {}", m);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, m);
        }

        try{
            String ownerId = apiUser.isUserIdValid(owner);
            dbGroup.setOwner(ownerId);

        }catch(HttpStatusCodeException e) {
            if(e.getStatusCode().is4xxClientError()) {
                String m = String.format("Suggested owner, %s, does not exist or is not valid", owner);
                log.warn("DbGROUP_SERVICE::updateOwner. {}", m);
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, m + " | " + e.getMessage());
            }else{
                throw e;
            }
        }
    }

    private void updatePolicy(DbGroup dbGroup, String value) throws HttpStatusCodeException {
        log.debug("DbGROUP_SERVICE::updatePolicy. $groupId: {} | $policy: {}", dbGroup.getId(), value);

        try{
            dbGroup.setPolicy(DbGroupPolicy.valueOf(value.toUpperCase()));
        }catch(IllegalArgumentException e) {
            log.error("DbGROUP_SERVICE::updatePolicy. {}", e.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
