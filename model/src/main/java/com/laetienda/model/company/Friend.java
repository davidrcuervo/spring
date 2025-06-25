package com.laetienda.model.company;

import com.laetienda.lib.options.FriendStatus;
import com.laetienda.model.schema.DbItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Friend extends DbItem {

    @NotNull
    @OneToOne
    @JoinColumn(name="friend_id")
    private Member friend;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FriendStatus status;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Friend(){}

    public Friend(Member friend, FriendStatus status){
        setFriend(friend);
        setStatus(status);
    }

    public FriendStatus getStatus() {
        return status;
    }

    public Friend setStatus(FriendStatus status) {
        this.status = status;
        return this;
    }

    public Member getFriend() {
        return friend;
    }

    public Friend setFriend(Member friend) {
        this.friend = friend;
        return this;
    }

    private Member getMember(){
        return this.member;
    }
}
