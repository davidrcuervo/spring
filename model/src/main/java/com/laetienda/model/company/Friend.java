package com.laetienda.model.company;

import com.laetienda.lib.options.CompanyFriendStatus;
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
    private CompanyFriendStatus status;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Friend(){}

    public Friend(Member friend, CompanyFriendStatus status){
        setFriend(friend);
        setStatus(status);
    }

    public CompanyFriendStatus getStatus() {
        return status;
    }

    public Friend setStatus(CompanyFriendStatus status) {
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
