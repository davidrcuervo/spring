package com.laetienda.company.service;

import com.laetienda.company.lib.EmailService;
import com.laetienda.company.repository.CompanyRepository;
import com.laetienda.company.repository.FriendRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CompanyFriendStatus;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Friend;
import com.laetienda.model.company.Member;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.smartcardio.CommandAPDU;
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
    public Friend find(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::find. $companyId: {} | memberUserId: {} | friendUserId: {}", companyId, memberUserId, friendUserId);

        List<Friend> friends = repo.find(companyId, memberUserId, friendUserId);
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
    public Friend add(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::add. $companyId: {} | memberUserId: {} | friendUserId: {}", companyId, memberUserId, friendUserId);

        try{
            Friend temp = find(companyId, memberUserId, friendUserId);
            String message = String.format("Friend already exists. $companyId: %s | memberUserId: %s | friendUserId: %s", companyId, memberUserId, friendUserId);
            log.warn("FRIEND_SERVICE::add. $message: {}", message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");

        }catch(NotValidCustomException e){
            if(e.getStatus().equals(HttpStatus.NOT_FOUND)){
                Long cid = serviceCompany.isCompanyValid(companyId);

                Member member = serviceCompany.findMemberByIds(companyId, memberUserId);
                if(!member.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
                    String message = String.format("Member need to be accepted by company before making friends. $companyId: %s | memberUserId: %s", companyId, memberUserId);
                    log.warn("FRIEND_SERVICE::add.. $message: {}", message);
                    throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "friend");
                }

                List<Member> temp = repoCompany.findMemberByUserIdNoJwt(cid, friendUserId);
                if(temp == null || temp.size() != 1){
                    String message = String.format("Friend is not member of the company. $companyId: %d | $friendUserId: %s", cid, friendUserId);
                    log.warn("FRIEND_SERVICE::add {}", message);
                    throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "friend");
                }

                Member memberFriend = temp.getFirst();
                if(!memberFriend.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
                    String message = String.format("Friend need to be accepted by company before making friends. $companyId: %d | friendUserId: %s", cid, friendUserId);
                    log.warn("FRIEND_SERVICE::add... {}", message);
                    throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "friend");
                }

                Friend friend = new Friend(member, memberFriend, CompanyFriendStatus.REQUESTED);
                friend.addEditor(friendUserId);
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
    public Friend update(Friend friend) throws NotValidCustomException {
        return repo.update(friend);
    }

    @Override
    public void delete(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException {
        log.debug("FRIEND_SERVICE::delete. $companyId: {} | memberUserId: {} | friendUserId: {}", companyId, memberUserId, friendUserId);
        Friend friend = find(companyId, memberUserId, friendUserId);
        repo.delete(friend);
    }
}
