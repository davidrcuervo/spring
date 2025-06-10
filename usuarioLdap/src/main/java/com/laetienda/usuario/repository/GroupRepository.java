package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import org.springframework.stereotype.Repository;

public interface GroupRepository {

    Group findByName(String name);
    GroupList findAll();
    GroupList findAllByOwner(String username);
    GroupList findAllByMember(String username);
    boolean isOwner(String gName, String username);
    boolean isMember(String gname, String username);

    GroupList findByMemberAndMember(String userA, String userB);
    Group setMembersdnAndOwnersdn(Group group);
}
