package com.laetienda.company.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Friend;
import com.laetienda.utils.service.api.ApiSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class FriendRepositoryImplementation implements FriendRepository {
    private final static Logger log = LoggerFactory.getLogger(FriendRepositoryImplementation.class);

    private final ApiSchema apiSchema;

    FriendRepositoryImplementation(ApiSchema apiSchema) {
        this.apiSchema = apiSchema;
    }

    @Override
    public List<Friend> find(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::find. $companyId: {} | $memberUserId: {} | $friendUserId: {}", companyId, memberUserId, friendUserId);
        String query = getFindQuery(companyId, memberUserId, friendUserId);

        return apiSchema.findByQuery(Friend.class, Map.of("query", query));
    }

    @Override
    public List<Friend> findByNoJwt(String companyId, String memberUserId, String friendUserId) {
        log.debug("FRIEND_REPOSITORY::findByNoJwt. $companyId: {} | $memberUserId: {} | $friendUserId: {}", companyId, memberUserId, friendUserId);
        String query = getFindQuery(companyId, memberUserId, friendUserId);

        return apiSchema.findByQueryByClientRegistrationId(
                Friend.class,
                Map.of("query", query)
        );
    }

    @Override
    public List<Friend> findAll(Long cid, String uid) {
        log.debug("FRIEND_REPOSITORY::findAll. $companyId: {} | $userId: {}", cid, uid);
        String query = getFindAllQuery(cid, uid);

        return apiSchema.findByQuery(Friend.class, Map.of("query", query));
    }

    @Override
    public Friend create(Friend friend) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::create. $companyId: {} | $memberUserId: {} | buddyUserId: {}", friend.getMember().getCompany().getId(), friend.getMember().getUserId(), friend.getBuddy().getUserId());
        return apiSchema.create(Friend.class, friend);
    }

    @Override
    public Friend update(Friend friend) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::update. $friendId: {}", friend.getId());
        return apiSchema.update(Friend.class, friend);
    }

    @Override
    public void delete(Friend friend) throws NotValidCustomException {
        log.debug("FRIEND_REPOSITORY::delete. $id: {} | $companyId: {} | memberUserId: {} | buddyUserId {}", friend.getId(), friend.getMember().getCompany().getId(), friend.getMember().getUserId(), friend.getBuddy().getUserId());
        apiSchema.deleteById(Friend.class, friend.getId());
    }

    private String getFindQuery(String companyId, String memberUserId, String buddyUserId) {
        return String.format("SELECT f FROM %s f INNER JOIN f.member m INNER JOIN f.buddy b INNER JOIN m.company c " +
                "WHERE (c.id = %s AND m.userId = '%s' AND b.userId = '%s') " +
                "OR (c.id = %s AND b.userId = '%s' AND m.userId = '%s')",
                Friend.class.getName(), companyId, memberUserId, buddyUserId,
                companyId, memberUserId, buddyUserId);
    }

    private String getFindAllQuery(Long cid, String uid) {
        return String.format("SELECT f FROM %s f INNER JOIN f.member m INNER JOIN f.buddy b INNER JOIN m.company c " +
                "WHERE (c.id = %d AND m.userId = '%s') " +
                        "OR (c.id = %d  AND b.userId = '%s')",
                Friend.class.getName(), cid, uid,
                cid, uid);
    }
}
