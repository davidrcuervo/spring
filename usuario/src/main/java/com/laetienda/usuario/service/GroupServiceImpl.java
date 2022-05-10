package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.usuario.repository.GroupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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
    public Group findGroupByName(String name) throws NotValidCustomException {
        Group result = repository.findByName(name);

        if(result == null){
            throw new NotValidCustomException("Group, (" + name + "), does not exist.", HttpStatus.NOT_FOUND, "name");
        }else if(repository.isOwner(result, USERNAME)){
            log.trace("Group succesfully found. $gname: {}", name);
        }else{
            throw new NotValidCustomException("User is not owner of the group", HttpStatus.UNAUTHORIZED, "group");
        }
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

    @Override
    public Group update(Group group, String gname) throws NotValidCustomException {
        Group result = repository.findByName(group.getName());
        Group temp = respository.findByName(gname);

        if(temp == null) {
            throw new NotValidCustomException("Group (" + gname + ") does not exist", HttpStatus.NOT_FOUND, "name");
        }else if (!gname.equalsIgnoreCase(group.getName()) && result != null ) {
            String message = String.format("Group, %s, already exists.", group.getName());
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "name");
        }else if(repository.isOwner(temp, USERNAME)){
            result = repository.update(group, gname, USERNAME);
        }else{
            throw new NotValidCustomException("User is not owner of group", HttpStatus.UNAUTHORIZED, "name");
        }

        return result;
    }

    @Override
    public Group delete(String gname) throws NotValidCustomException {
        Group result = respository.findByName(gname);

        if(result == null){
            String message = String.format("Group, (name: %s), does not exist.", gname);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "name");
        }else if(repository.isOwner(result, USERNAME)){
            result = repository.delete(result);
        }else{
            throw new NotValidCustomException("User is not owner, and can't remove the group", HttpStatus.UNAUTHORIZED, "group");
        }

        return result;
    }
}
