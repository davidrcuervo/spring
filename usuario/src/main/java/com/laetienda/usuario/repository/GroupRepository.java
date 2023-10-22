package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository {

//    GroupList findAllByOwner(Usuario owner);
    Group findByName(String name);
//    boolean isOwner(Group group, String username);
//    boolean isMember(Group group, String username);
//    GroupList findAllByMember(Usuario user);
//    Group addMember(String gname, String username);
//    Group addOwner(String gname, String username);
//    Group removeMember(Group group, Usuario user) throws IOException;
//    Group removeOwner(Group group, Usuario user) throws IOException;
//    Group create(Group group, String owner);
//    Group update(Group group, String name, String username);
//    Group delete(Group group);
    GroupList findAll();
    GroupList findAllByOwner(String username);
    GroupList findAllByMember(String username);
    boolean isOwner(String gName, String username);
    boolean isMember(String gname, String username);

    GroupList findByMemberAndMember(String userA, String userB);
}
