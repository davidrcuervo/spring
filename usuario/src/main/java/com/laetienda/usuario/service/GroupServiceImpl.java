package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.usuario.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpSession;
import java.util.List;

public class GroupServiceImpl implements GroupService{
    final private static Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

    final private String USERNAME = "admuser";

    @Autowired
    private GroupRepository respository;

    @Autowired
    private GroupRepository repository;

    @Autowired
    private HttpSession session;

    @Override
    public Group findGroupByName(String name) {
        Group result = repository.findByName(name);

//        if(repository.isOwner(result, USERNAME)){
//
//        }else{
//            result = null;
//        }
        return result;
    }

    @Override
    public List<Group> findAll() {
        return repository.findAll(USERNAME);
    }

    @Override
    public Group create(Group group) throws NotValidCustomException {
        Group result = null;

        if(repository.findByName(group.getName()) != null){
            log.trace("Can't create group. Group name already exists. $name: {}", group.getName());
            throw new NotValidCustomException("Group already exists", HttpStatus.BAD_REQUEST, "name");
        }else{
            result = repository.create(group, USERNAME);
        }

        return result;
    }
}
