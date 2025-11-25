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
    private Member buddy;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CompanyFriendStatus status;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Friend(){}

    public Friend(Member member, Member buddyMember, CompanyFriendStatus status){
        setBuddy(buddyMember);
        setStatus(status);
        setMember(member);
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public CompanyFriendStatus getStatus() {
        return status;
    }

    public Friend setStatus(CompanyFriendStatus status) {
        this.status = status;
        return this;
    }

    public Member getBuddy() {
        return buddy;
    }

    public Friend setBuddy(Member buddy) {
        this.buddy = buddy;
        return this;
    }

    public Member getMember(){
        return this.member;
    }
}
