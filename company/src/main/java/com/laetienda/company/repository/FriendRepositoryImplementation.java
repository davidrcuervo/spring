package com.laetienda.company.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Friend;
import com.laetienda.utils.service.api.ApiSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FriendRepositoryImplementation implements FriendRepository {
    private final static Logger log = LoggerFactory.getLogger(FriendRepositoryImplementation.class);

    @Autowired ApiSchema apiSchema;
    @Autowired ObjectMapper json;

    @Override
    public List<Friend> find(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::find. $companyId: {} | $memberUserId: {} | $friendUserId: {}", companyId, memberUserId, friendUserId);
        String query = getFindQuery(companyId, memberUserId, friendUserId);

        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);

        ResponseEntity<String> response = apiSchema.findByQuery(Friend.class, params);
        return findByQuery(response);
    }

    @Override
    public List<Friend> findByNoJwt(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::findByNoJwt. $companyId: {} | $memberUserId: {} | $friendUserId: {}", companyId, memberUserId, friendUserId);
        String query = getFindQuery(companyId, memberUserId, friendUserId);

        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);

        ResponseEntity<String> response = apiSchema.findByQueryNoJwt(Friend.class, params);
        return findByQuery(response);
    }

    @Override
    public List<Friend> findAll(Long cid, String uid) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::findAll. $companyId: {} | $userId: {}", cid, uid);

        String query = getFindAllQuery(cid, uid);
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);

        ResponseEntity<String> response = apiSchema.findByQuery(Friend.class, params);
        return findByQuery(response);
    }

    @Override
    public Friend create(Friend friend) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::create. $companyId: {} | $memberUserId: {} | buddyUserId: {}", friend.getMember().getCompany().getId(), friend.getMember().getUserId(), friend.getBuddy().getUserId());
        return apiSchema.create(Friend.class, friend).getBody();
    }

    @Override
    public Friend update(Friend friend) throws NotValidCustomException {
        return null;
    }

    @Override
    public void delete(Friend friend) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::delete. $id: {} | $companyId: {} | memberUserId: {} | buddyUserId {}", friend.getId(), friend.getMember().getCompany().getId(), friend.getMember().getUserId(), friend.getBuddy().getUserId());
        apiSchema.deleteById(Friend.class, friend.getId());
    }

    private List<Friend> findByQuery(ResponseEntity<String> response) throws NotValidCustomException {
        try {
            return json.readValue(response.getBody(), new TypeReference<List<Friend>>(){});
        } catch (JsonProcessingException e) {
            String message = String.format("FRIEND_REPOSITORY::findByQuery. $error: %s", e.getMessage());
            log.error(message);
            log.trace(message, e);
            throw new NotValidCustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "friend");
        }
    }

    private String getFindQuery(String companyId, String memberUserId, String buddyUserId) throws NotValidCustomException{
        return String.format("SELECT f FROM %s f INNER JOIN f.member m INNER JOIN f.buddy b INNER JOIN m.company c " +
                "WHERE (c.id = %s AND m.userId = '%s' AND b.userId = '%s') " +
                "OR (c.id = %s AND b.userId = '%s' AND m.userId = '%s')",
                Friend.class.getName(), companyId, memberUserId, buddyUserId,
                companyId, memberUserId, buddyUserId);
    }

    private String getFindAllQuery(Long cid, String uid) throws NotValidCustomException{
        return String.format("SELECT f FROM %s f INNER JOIN f.member m INNER JOIN f.buddy b INNER JOIN m.company c " +
                "WHERE (c.id = %d AND m.userId = '%s') " +
                        "OR (c.id = %d  AND b.userId = '%s')",
                Friend.class.getName(), cid, uid,
                cid, uid);
    }
}
