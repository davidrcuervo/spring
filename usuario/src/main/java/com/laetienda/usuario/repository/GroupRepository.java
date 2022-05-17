package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Repository
public interface GroupRepository {
    Group create(Group group, String owner);
    Group findByName(String name);
    Map<String, Group> findAll(Usuario owner);
    Map<String, Group> findAll(String username);
    Group update(Group group, String name, String username);
    Group delete(Group group);
    boolean isOwner(Group group, String username);
    boolean isMember(Group group, String username);
    Group addMember(Group group, Usuario user);
    Group removeMember(Group group, Usuario user) throws IOException;

    Group addOwner(Group group, Usuario user);

    Group removeOwner(Group group, Usuario user) throws IOException;
}
