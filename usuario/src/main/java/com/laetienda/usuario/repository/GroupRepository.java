package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Repository
public interface GroupRepository {

    /**
     *
     * @param group
     * @param owner
     * @return
     */
    Group create(Group group, String owner);

    /**
     *
     * @return all groups in LDAP
     */
    GroupList findAll();
    Group findByName(String name);
    GroupList findAllByOwner(Usuario owner);
    GroupList findAllByOwner(String username);
    GroupList findAllByMemberAndMember(String userA, String userB);
    Group update(Group group, String name, String username);
    Group delete(Group group);
    boolean isOwner(Group group, String username);
    boolean isOwner(String gName, String username);
    boolean isMember(String gname, String username);
    boolean isMember(Group group, String username);
    Group addMember(Group group, Usuario user);
    Group removeMember(Group group, Usuario user) throws IOException;

    Group addOwner(Group group, Usuario user);

    Group removeOwner(Group group, Usuario user) throws IOException;

    GroupList findAllByMember(Usuario user);
    GroupList findAllByMember(String username);
}
