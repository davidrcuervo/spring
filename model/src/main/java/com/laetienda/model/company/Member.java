package com.laetienda.model.company;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.laetienda.lib.options.FriendStatus;
import com.laetienda.model.schema.DbItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import net.minidev.json.annotate.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Member extends DbItem {
    private final static Logger log = LoggerFactory.getLogger(Member.class);

    @NotNull
    private String userId;

    @ManyToOne
    @JoinColumn(name="company_id")
    private Company company;

    @JsonIgnore
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friend> friends = new ArrayList<Friend>();

    public Member(Company company, String memberId, String ownerId) {
        super.setOwner(ownerId);
        super.addEditor(memberId);
        this.company = company;
        this.userId = memberId;
    }

    public String getUserId() {
        return userId;
    }

    public Member setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    @JsonIgnore
    public Member submitFriendRequest(@NotNull Member member){
        if (friends == null) {
            this.friends = new ArrayList<>();
        }

        if(friends.stream().anyMatch(f -> f.getFriend().getUserId().equals(member.getUserId()))){
            log.warn("MODEL_MEMBER::submitFriendRequest. Friend already exists. $member: {} | $friend: {} | $company: {}", this.userId, member.getUserId(), company.getName());
        }else{
            Friend friend = new Friend(member, FriendStatus.REQUEST_SUBMITTED);
            friends.add(friend);
        }
        return this;
    }

    @JsonIgnore
    public Member receiveFriendRequest(@NotNull Member member){
        if (friends == null) {
            this.friends = new ArrayList<>();
        }

        if(friends.stream().anyMatch(f -> f.getFriend().getUserId().equals(member.getUserId()))){
            log.warn("MODEL_MEMBER::receiveFriendRequest. Friend already exists. $member: {} | $friend: {} | $company: {}", this.userId, member.getUserId(), company.getName());
        }else{
            Friend friend = new Friend(member, FriendStatus.REQUEST_RECEIVED);
            friends.add(friend);
        }
        return this;
    }

    @JsonIgnore
    public Member acceptFriend(@NotNull Member member) throws IOException {

        if(friends != null
                && friends.stream().filter(f -> f.getStatus().equals(FriendStatus.REQUEST_RECEIVED))
                .anyMatch(f -> f.getFriend().getUserId().equals(member.getUserId()))
        ){
            List<Friend> temp = friends.stream().filter(f -> f.getFriend().getUserId().equals(member.getUserId())).toList();

            if(temp.size() > 1){
                String message = "There are more than 1 friend with same user_id.";
                log.error(message);
                throw new IOException();
            }

            if(temp.getFirst().getStatus().equals(FriendStatus.REQUEST_RECEIVED)){
                this.friends.stream()
                        .filter(f -> f.getFriend().getUserId().equals(member.getUserId()))
                        .filter(f -> f.getStatus().equals(FriendStatus.REQUEST_RECEIVED))
                        .forEach(f -> f.setStatus(FriendStatus.ACCEPTED));
            }

        }else{
            log.warn("MODEL_MEMBER::acceptFriend. Friend has not submitted request. $member: {} | $friend: {}, $company: {}", this.userId, member.getUserId(), this.company.getName());
        }
        return this;
    }

    @JsonIgnore
    public Member submitFriendRequestHasBeenAccepted(@NotNull Member member) throws IOException{
        if(friends != null
                && friends.stream().filter(f -> f.getStatus().equals(FriendStatus.REQUEST_SUBMITTED))
                .anyMatch(f -> f.getFriend().getUserId().equals(member.getUserId()))
        ){
            List<Friend> temp = friends.stream().filter(f -> f.getFriend().getUserId().equals(member.getUserId())).toList();

            if(temp.size() > 1){
                String message = "There are more than 1 friend with same user_id";
                log.error(message);
                throw new IOException(message);
            }

            if(temp.getFirst().getStatus().equals(FriendStatus.REQUEST_SUBMITTED)){
                this.friends.stream()
                        .filter(f -> f.getFriend().getUserId().equals(member.getUserId()))
                        .filter(f -> f.getStatus().equals(FriendStatus.REQUEST_SUBMITTED))
                        .forEach(f -> f.setStatus(FriendStatus.ACCEPTED));
            }

        }else{
            log.warn("MODEL_MEMBER::submitFriendRequestHasBeenAccepted. Friend request has not been submitted. $member: {} | $friend: {}, $company: {}", this.userId, member.getUserId(), this.company.getName());
        }
        return this;
    }

    @JsonIgnore
    public Member blockFriend(@NotNull Member member) throws IOException {
        List<Friend> temp = friends.stream()
                .filter(f -> f.getFriend().getUserId().equals(member.getUserId()))
                .toList();

        if(temp.size() > 1){
            String message = String.format("There are more than 1 friendship instance. $member: %s | $friend: %s", userId, member.getUserId());
            log.error("MODEL_MEMBER::blockFriend. {}", message);
            throw new IOException(message);
        }

        if(temp.size() == 1){
            friends.stream()
                    .filter(f -> f.getFriend().getUserId().equals(member.getUserId()))
                    .forEach(f -> f.setStatus(FriendStatus.BLOCKED));
        }

        return this;
    }

    @JsonIgnore
    public Member deleteFriend(@NotNull Member member) throws IOException {
        List<Friend> temp = friends.stream()
                .filter(f -> f.getFriend().getUserId().equals(member.getUserId()))
                .toList();

        if(temp.size() > 1){
            String message = String.format("There are more than 1 friendship instance. $member: %s | $friend: %s", userId, member.getUserId());
            log.error("MODEL_MEMBER::deleteFriend. {}", message);
            throw new IOException(message);
        }

        if(temp.size() == 1){
            friends.removeIf(f -> f.getFriend().getUserId().equals(member.getUserId()));
        }

        return this;
    }

    @JsonIgnore
    public List<Friend> getFriends(){
        return friends.stream()
                .filter(f -> f.getStatus().equals(FriendStatus.ACCEPTED))
                .toList();
    }

    @JsonIgnore
    public List<Friend> getFriendRequestSubmitted(){
        return friends.stream()
                .filter(f -> f.getStatus().equals(FriendStatus.REQUEST_SUBMITTED))
                .toList();
    }

    @JsonIgnore
    public Member setFriendRequestSubmitted(List<Friend> friendRequestSubmitted){
        return this;
    }

    @JsonIgnore
    public List<Friend> getFriendRequestReceived(){
        return friends.stream()
                .filter(f -> f.getStatus().equals(FriendStatus.REQUEST_RECEIVED))
                .toList();
    }

    @JsonIgnore
    public Member setFriendRequestReceived(List<Friend> friendRequestReceived){
        return this;
    }
}
