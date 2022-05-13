package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GroupService {

    public Group findGroupByName(String name) throws NotValidCustomException;
    public List<Group> findAll();
    public Group create(Group group) throws NotValidCustomException;
    public Group update(Group group, String gname) throws NotValidCustomException;
    public Group delete(String gname) throws NotValidCustomException;

    Boolean isMember(String gname, String username) throws NotValidCustomException;

    public Group addMember(String gname, String username) throws NotValidCustomException;

    public Group removeMember(String gname, String username) throws NotValidCustomException;

    Group addOwner(String gname, String username) throws NotValidCustomException;

    Group removeOwner(String gname, String username) throws NotValidCustomException;
}
