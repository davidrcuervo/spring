package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.SpringGroupRepository;
import com.laetienda.usuario.repository.SpringUserRepository;
import com.laetienda.usuario.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import javax.naming.Name;
import java.io.IOException;
import java.util.*;

public class GroupServiceImpl implements GroupService{
    final private static Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

//    final private String USERNAME = "admuser";

    @Autowired
    private GroupRepository repository;

    @Autowired
    private SpringGroupRepository springGroupRepository;

    @Autowired
    private HttpSession session;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private SpringUserRepository springUserRepository;

    @Autowired
    private LdapDn dn;

    @Override
    public Group findByName(String gname) throws NotValidCustomException {
        Group result = repository.findByName(gname);
        String username = getLoggedUser();

        if(result == null) {
            throw new NotValidCustomException("Group, (" + gname + "), does not exist.", HttpStatus.NOT_FOUND, "name");
        }else if(repository.isMember("manager", username)){
            log.trace("Group successfully, user is manager");

        }else if(repository.isMember(gname, username)){
            log.trace("Group successfully found. $gname: {}", gname);
        }else{
            throw new NotValidCustomException("User is not member of the group", HttpStatus.UNAUTHORIZED, "user");
        }

        return result;
//        findOwners(result);
//        findMembers(result);

//        return buildGroup(result);
    }

//    private Group buildGroup(Group group){
//        findMembers(group);
//        findOwners(group);
//        return group;
//    }

//    private GroupList buildGroupList(GroupList groups){
//        groups.getGroups().forEach((gname, group) -> {
//            findOwners(group);
//            findMembers(group);
//        });
//
//        return groups;
//    }

//    private Group findOwners(Group group) {
//        Map<String, Usuario> owners = new HashMap<>();
//        log.trace("# of owners: {}", group.getOwnersdn().size());
//        group.getOwnersdn().forEach(
//                (ownerdn) -> {
//                    String username = ownerdn.split(",")[0].split("=")[1];
//                    owners.put(username, springUserRepository.findByUsername(username));
//                });
//        group.setOwners(owners);
//        return group;
//    }

//    private Group findMembers(Group result) {
//        Map<String, Usuario> members = new HashMap<>();
//        log.trace("# of members: {}", result.getMembersdn().size());
//        result.getMembersdn().forEach(
//                (memberdn) -> {
//                    String username = memberdn.split(",")[0].split("=")[1];
//                    members.put(username, springUserRepository.findByUsername(username));
//                });
//        result.setMembers(members);
//        return result;
//    }

    @Override
    public GroupList findAll() {
        GroupList result = new GroupList();

        //Return all groups if user is manager
        if (repository.isMember("manager", getLoggedUser())) {
            result = repository.findAll();

        //Return groups where user is member
        } else {
            result = repository.findAllByMember(getLoggedUser());
        }

        return result;
    }

    @Override
    public GroupList findAllByMember(String username) throws NotValidCustomException {
        String loggedUsername = getLoggedUser();
        GroupList result = null;

        if(repository.isMember("manager", loggedUsername)){
            result = repository.findAllByMember(username);

        }else{
            result = repository.findByMemberAndMember(username, loggedUsername);

        }

        return result;
    }

    @Override
    public Group create(Group group) throws NotValidCustomException {
        Group result = null;

        if(repository.findByName(group.getName()) != null){
            log.trace("Can't create group. Group name already exists. $name: {}", group.getName());
            throw new NotValidCustomException("Group already exists", HttpStatus.BAD_REQUEST, "name");
        }else{
            Usuario owner = springUserRepository.findByUsername(getLoggedUser());
            Name groupdn = dn.getGroupDn(group.getName());
            group.setDn(groupdn);
            group.addOwner(owner);
            group.setNew(true);
            result = springGroupRepository.save(group);
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

        }else if(repository.isOwner(temp.getName(), getLoggedUser())){
//            result = repository.update(group, gname, getLoggedUser());
            Name olddn = dn.getGroupDn(gname);
            Name newdn = dn.getGroupDn(group.getName());

            if(gname.equals(group.getName())){
                group.setDn(olddn);
                result = springGroupRepository.save(group);
            }else{

                result.setName(group.getName());
                result.setDn(newdn);
                springGroupRepository.save(result);

                Group invalid = springGroupRepository.findByName(gname);
                springGroupRepository.delete(invalid);

                result = update(result, result.getName());
            }

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

        //TODO check the GroupRepoImpl

        return null;
    }

    @Override
    public Boolean isMember(String gName, String username) throws NotValidCustomException {
        boolean result = false;
        String loggedUser = getLoggedUser();

        if(springGroupRepository.findByName(gName) == null) {
            throw new NotValidCustomException("Group does not exist", HttpStatus.NOT_FOUND, "group");

        }else if(repository.isMember("manager", loggedUser)){
            log.trace("User is manager and it is authorized to run isMember");

        }else if(repository.isMember(gName, loggedUser)){
            log.trace("User is member and it is authorized to run isMember");

        }else{
            throw new NotValidCustomException("User is unauthorized to find if it is member.", HttpStatus.UNAUTHORIZED, "user");
        }

        return repository.isMember(gName, username);
    }

    @Override
    public Group addMember(String gname, String username) throws NotValidCustomException {
        if(gname == null || username == null){
            throw new NotValidCustomException("Group name or username is missing.", HttpStatus.BAD_REQUEST, "group");
        }

        Group group = getGroupOwner(gname);
        Usuario member = getUser(username);

        group.addMember(member);
        return springGroupRepository.save(group);
//        return repository.addMember(group, user);
    }

//    @Override
//    public Group addMemberToValidUserAccounts(String username) throws NotValidCustomException{
//        return addMember("validUserAccounts", username);
//    }

    private Usuario getUser(String username) throws NotValidCustomException {
        Usuario result = springUserRepository.findByUsername(username);

        if(result == null){
            String message = String.format("User, %s, does not exist, it is not possible to add to group.", username);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "username");
        }

        return result;
    }

    @Override
    public Group removeMember(String gname, String username) throws NotValidCustomException {
        Group result = null;
        String authUsername = request.getUserPrincipal().getName();

        try {
            //test if is owner OR self member
            if(repository.isOwner(gname, authUsername) || authUsername.equals(username)){
                //remove
                Group group = repository.findByName(gname);
                Usuario user = springUserRepository.findByUsername(username);
                group.removeMember(user);

                //persist
                result = springGroupRepository.save(group);
            }else{
                String message = String.format("'%s' user is not authorized to remove '%s' user from '%s' group", authUsername, username, gname);
                throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "user");
            }
        } catch (IOException e) {
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "user");
        }

        return result;

//        try{
//            Group group = getGroupOwner(gname);
//            Usuario user = getUser(username);
//            group.removeMember(user);
//
//            result = springGroupRepository.save(group);
//        }catch(IOException e){
//            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "user");
//        }

//        return result;
    }

    @Override
    public Group addOwner(String gname, String username) throws NotValidCustomException {
        Group group = getGroupOwner(gname);
        Usuario user = getUser(username);
        group.addOwner(user);
        return springGroupRepository.save(group);
    }

    @Override
    public Group removeOwner(String gname, String username) throws NotValidCustomException {
        Group result = null;

        try {
            Group group = getGroupOwner(gname);
            Usuario user = getUser(username);
            group.removeOwner(user);
            result = springGroupRepository.save(group);
        }catch (IOException e){
            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "group");
        }

        return result;
    }

    @Override
    public GroupList findAllByMember(Usuario user) {
        return repository.findAllByMember(user.getUsername());
    }

    @Override
    public GroupList testSpringLdapGroup(String username) {

        return repository.findAllByOwner(username);
    }

    private String getLoggedUser(){
        String result = request.getUserPrincipal().getName();
        log.trace("Finding logged user. $username: {}", result);
        return result;
    }


    private Group getGroupOwner(String gname) throws NotValidCustomException {
        Group result = repository.findByName(gname);
        String username = getLoggedUser();

        if(result == null){
            String message = String.format("Group, (name: %s), does not exist.", gname);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "name");

        }else if(repository.isOwner(gname, username)){
            log.debug("Group, ({}), found and privileges are granted", gname);
        }else{
            String message = String.format("User, %s, is not owner of group, %s, and can't edit the group", username, gname);
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "group");
        }

        return result;
    }
}
