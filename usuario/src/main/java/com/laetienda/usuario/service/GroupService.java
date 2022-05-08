package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface GroupService {

    public Group findGroupByName(String name);
    public List<Group> findAll();
    public Group create(Group group) throws NotValidCustomException;
}
