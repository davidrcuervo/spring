package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;

@Repository
public interface GroupRepository {
    public Group create(Group group, String owner);
    public Group findByName(String name);
    public List<Group> findAll(Usuario owner);
    public List<Group> findAll(String username);
    public Group update(Group group, String name, String username);
    public Group delete(Group group);

    public boolean isOwner(Group group, String username);
    public boolean isMember(Group group, String username);

    public Group addMember(Group group, Usuario user);

    public Group removeMember(Group group, Usuario user) throws IOException;

    Group addOwner(Group group, Usuario user);

    Group removeOwner(Group group, Usuario user) throws IOException;
}
