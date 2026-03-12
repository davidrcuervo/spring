package com.laetienda.company.service;

import com.laetienda.company.lib.EmailService;
import com.laetienda.company.repository.CompanyRepository;
import com.laetienda.company.repository.FriendRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CompanyFriendStatus;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Friend;
import com.laetienda.model.company.Member;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendServiceImplementation implements FriendService{
    private final static Logger log = LoggerFactory.getLogger(FriendServiceImplementation.class);

    @Autowired private FriendRepository repo;
    @Autowired private CompanyService serviceCompany;
    @Autowired private CompanyRepository repoCompany;
    @Autowired private ApiUser apiUser;
    @Autowired private EmailService email;

    @Override
    public Friend find(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::find. $companyId: {} | friendUserId: {}", companyId, userId);
        String currentUserId = apiUser.getCurrentUserId();

        if(currentUserId.equals(userId)){
            String message = String.format("It is forbidden that user has the same id than friend. $userId: %s", userId);
            log.warn("FRIEND_SERVICE::find. $error: {}", message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");
        }

        List<Friend> friends = repo.find(companyId, currentUserId, userId);

        return find(friends);
    }

    @Override
    public List<Friend> findAll(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::findAll. $companyId: {} | $userId: {}", companyId, userId);
        Long cid = serviceCompany.isCompanyValid(companyId);
        String uid = apiUser.isUserIdValid(userId);

        return repo.findAll(cid, userId);
    }

    private Friend findByNoJwt(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::findByNoJwt. $companyId: {} | memberUserId: {} | friendUserId: {}", companyId, memberUserId, friendUserId);

        List<Friend> friends = repo.findByNoJwt(companyId, memberUserId, friendUserId);
        return find(friends);
    }

    private Friend find(List<Friend> friends) throws NotValidCustomException{
        if(friends == null || friends.isEmpty()){
            String message = "Friend does not exist or current user does not have privileges to read it.";
            log.warn("FRIEND_SERVICE::find. $message: {}", message);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "friend");
        }

        if(friends.size() > 1){
            String message = "This friend is repeated. Huge error here.";
            log.error("FRIEND_SERVICE::find. $message: {}", message);
            throw new NotValidCustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "friend");
        }

        return friends.getFirst();
    }

    @Override
    public Friend add(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::add. $companyId: {} | userId: {}", companyId, userId);

        try{
            Friend temp = find(companyId, userId);
            String message = String.format("Friend already exists. $companyId: %s | memberUserId: %s | friendUserId: %s", companyId, temp.getMember().getUserId(), userId);
            log.warn("FRIEND_SERVICE::add. $message: {}", message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");

        }catch(NotValidCustomException e){
            if(e.getStatus().equals(HttpStatus.NOT_FOUND)){
                Long cid = serviceCompany.isCompanyValid(companyId);
                String currentUserId = apiUser.getCurrentUserId();

                Member member = serviceCompany.findMemberByIds(companyId, currentUserId);
                if(!currentUserId.equals(member.getUserId())){
                    String message = String.format("Current user is different to member requesting friendship. $currentUser: %s | $memberUserId: %s", currentUserId, member.getUserId());
                    log.warn("FRIEND_SERVICE::add, $message: {}", message);
                    throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "friend");
                }

                if(!member.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
                    String message = String.format("Member need to be accepted by company before making friends. $companyId: %s | memberUserId: %s", companyId, currentUserId);
                    log.warn("FRIEND_SERVICE::add.. $message: {}", message);
                    throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");
                }

                List<Member> temp = repoCompany.findMemberByUserIdNoJwt(cid, userId);
                if(temp == null || temp.size() != 1){
                    String message = String.format("Friend is not member of the company. $companyId: %d | $friendUserId: %s", cid, userId);
                    log.warn("FRIEND_SERVICE::add {}", message);
                    throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "friend");
                }

                Member memberFriend = temp.getFirst();
                if(!memberFriend.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
                    String message = String.format("Friend need to be accepted by company before making friends. $companyId: %d | friendUserId: %s", cid, userId);
                    log.warn("FRIEND_SERVICE::add... {}", message);
                    throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "friend");
                }

                Friend friend = new Friend(member, memberFriend, CompanyFriendStatus.REQUESTED);
                friend.addEditor(userId);
                friend.addEditor(member.getCompany().getOwner());
                Friend result = repo.create(friend);

                email.sendFriendRequest(result);
                return result;
            }else{
                throw e;
            }
        }
    }

    @Override
    public void delete(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::delete. $companyId: {} | userId: {}", companyId, userId);
        Friend friend = find(companyId, userId);
        repo.delete(friend);
    }

    @Override
    public Friend accept(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::accept. $companyId: {} | $buddyUserId: {}", companyId, userId);

        Friend friend = find(companyId, userId);

        if(!friend.getStatus().equals(CompanyFriendStatus.REQUESTED)){
            String message = String.format("Friend status is not requested. Friend can't be accepted. $status: %s", friend.getStatus());
            log.warn("FRIEND_SERVICE::accept. $error: {}", message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");
        }

        friend.setStatus(CompanyFriendStatus.ACCEPTED);
        Friend result = repo.update(friend);
        email.acceptFriendRequest(result);
        return result;
    }

    @Override
    public Friend block(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::block. $companyId: {} | $userId: {}", companyId, userId);
        String currentUserId = apiUser.getCurrentUserId();

        Friend friend = find(companyId, userId);
        CompanyFriendStatus status = friend.getStatus();

        if(status.equals(CompanyFriendStatus.BLOCKED_BY_SENDER) || status.equals(CompanyFriendStatus.BLOCKED_BY_RECEIVER)) {
            return friend;

        }else{
             if (currentUserId.equals(friend.getMember().getUserId())) {
                friend.setStatus(CompanyFriendStatus.BLOCKED_BY_SENDER);

            } else if (currentUserId.equals(friend.getBuddy().getUserId())) {
                friend.setStatus(CompanyFriendStatus.BLOCKED_BY_RECEIVER);
            }
            return repo.update(friend);
        }
    }

    @Override
    public Friend unblock(String companyId, String userId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::unblock. $companyId: {} | $userId: {}", companyId, userId);
        String currentUserId = apiUser.getCurrentUserId();

        Friend friend = find(companyId, userId);
        CompanyFriendStatus status = friend.getStatus();

        if(status.equals(CompanyFriendStatus.BLOCKED_BY_RECEIVER)
                || status.equals(CompanyFriendStatus.BLOCKED_BY_SENDER)){

            if(status.equals(CompanyFriendStatus.BLOCKED_BY_RECEIVER)
                    && currentUserId.equals(friend.getBuddy().getUserId())){
                friend.setStatus(CompanyFriendStatus.ACCEPTED);

            }else if(status.equals(CompanyFriendStatus.BLOCKED_BY_SENDER)
                    && currentUserId.equals(friend.getMember().getUserId())){
                friend.setStatus(CompanyFriendStatus.ACCEPTED);

            }else{
                String message = "Not possible to unblock friend.";
                log.warn("FRIEND_SERVICE::unblock. $error: {}", message);
                throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");
            }

            return repo.update(friend);

        }else{
            String message = String.format("Friend is not blocked. $friendStatus: %s", status);
            log.trace("FRIEND_SERVICE::unblock. $error: {}", message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "friend");
        }
    }
}
