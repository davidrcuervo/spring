package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

public class GroupServiceImpl implements GroupService{
    final private static Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

    final private String USERNAME = "admuser";

    @Autowired
    private GroupRepository repository;

    @Autowired
    private HttpSession session;

    @Autowired
    private UserRepository userRepo;

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
        Group temp = repository.findByName(gname);

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

        if (gname.equalsIgnoreCase("manager") || gname.equalsIgnoreCase("validUserAccounts")){
            String message = String.format("Group, (%s), can not be removed", gname);
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "group");
        }

        Group result = getGroupOwner(gname);
        result = repository.delete(result);

        return result;
    }

    @Override
    public Boolean isMember(String gname, String username) throws NotValidCustomException {
        Group group = getGroupOwner(gname);
        boolean result = repository.isMember(group, username);

        return Boolean.valueOf(result);
    }

    @Override
    public Group addMember(String gname, String username) throws NotValidCustomException {
        if(gname == null || username == null){
            throw new NotValidCustomException("Group name or username is missing.", HttpStatus.BAD_REQUEST, "group");
        }

        Group group  = getGroupOwner(gname);
        Usuario user = getUser(username);

        return repository.addMember(group, user);
    }

    private Usuario getUser(String username) throws NotValidCustomException {
        Usuario result = userRepo.find(username);

        if(result == null){
            throw new NotValidCustomException("User does not exist, it is not possible to add to group.", HttpStatus.BAD_REQUEST, "username");
        }

        return result;
    }

    @Override
    public Group removeMember(String gname, String username) throws NotValidCustomException {
        Group result = null;

        try{
            Group group = getGroupOwner(gname);
            Usuario user = getUser(username);
            result = repository.removeMember(group, user);
        }catch(IOException e){
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "user");
        }

        return result;
    }

    @Override
    public Group addOwner(String gname, String username) throws NotValidCustomException {
        Group group = getGroupOwner(gname);
        Usuario user = getUser(username);
        return repository.addOwner(group, user);
    }

    @Override
    public Group removeOwner(String gname, String username) throws NotValidCustomException {
        Group result = null;

        try {
            Group group = getGroupOwner(gname);
            Usuario user = getUser(username);
            result = repository.removeOwner(group, user);
        }catch (IOException e){
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "group");
        }

        return result;
    }

    private Group getGroupOwner(String gname) throws NotValidCustomException {
        Group result = repository.findByName(gname);

        if(result == null){
            String message = String.format("Group, (name: %s), does not exist.", gname);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "name");
        }else if(repository.isOwner(result, USERNAME)){
            log.debug("Group, ({}), found and privileges are granted", gname);
        }else{
            throw new NotValidCustomException("User is not owner, and can't remove the group", HttpStatus.UNAUTHORIZED, "group");
        }

        return result;
    }
}
