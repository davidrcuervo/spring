package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface GroupService {

    Group findGroupByName(String name) throws NotValidCustomException;
    Map<String, Group> findAll();
    Group create(Group group) throws NotValidCustomException;
    Group update(Group group, String gname) throws NotValidCustomException;
    Group delete(String gname) throws NotValidCustomException;
    Boolean isMember(String gname, String username) throws NotValidCustomException;
    Group addMember(String gname, String username) throws NotValidCustomException;
    Group removeMember(String gname, String username) throws NotValidCustomException;
    Group addOwner(String gname, String username) throws NotValidCustomException;
    Group removeOwner(String gname, String username) throws NotValidCustomException;
}
