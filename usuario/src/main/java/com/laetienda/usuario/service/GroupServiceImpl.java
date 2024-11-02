package com.laetienda.usuario.service;

import com.laetienda.lib.exception.CustomRestClientException;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.SpringGroupRepository;
import com.laetienda.usuario.repository.SpringUserRepository;
import com.laetienda.usuario.repository.UserRepository;
import org.mockito.internal.matchers.Not;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import javax.naming.Name;
import java.io.IOException;
import java.util.*;

public class GroupServiceImpl implements GroupService{
    final private static Logger log = LoggerFactory.getLogger(GroupServiceImpl.class);

    @Value("${admuser.username}")
    private String admuser;

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
    }

    @Override
    public GroupList findAll() throws NotValidCustomException {

        //Only managers can't fetch all groups
        if (repository.isMember("manager", getLoggedUser())) {
            return repository.findAll();

        } else {
            return repository.findAllByMember(getLoggedUser());
        }
    }

    @Override
    public GroupList findAllByMember(String username) throws NotValidCustomException {
        String loggedUsername = getLoggedUser();
        GroupList result = null;
        Usuario user = springUserRepository.findByUsername(username);

        if(user == null){
            String message = String.format("User, %s, does not exist.", username);
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "user");
        }else if(repository.isMember("manager", loggedUsername)){
            result = repository.findAllByMember(username);

        }else{
            result = repository.findByMemberAndMember(username, loggedUsername);
        }

        return result;
    }

    @Override
    public Group create(Group group) throws NotValidCustomException {

        if(springGroupRepository.findByName(group.getName()) != null){
            log.trace("Can't create group. Group name already exists. $name: {}", group.getName());
            throw new NotValidCustomException("Group already exists", HttpStatus.BAD_REQUEST, "name");
        }else{
            Usuario owner = springUserRepository.findByUsername(getLoggedUser());
            group = repository.setMembersdnAndOwnersdn(group);
            group.addOwner(owner);
            group.setNew(true);
            springGroupRepository.save(group);
        }

        return repository.findByName(group.getName());
    }

    @Override
    public Group update(Group group, String gname) throws NotValidCustomException {
        Group result = repository.findByName(group.getName());
        Group temp = repository.findByName(gname);

        if(temp == null) {
            throw new NotValidCustomException("Group (" + gname + ") does not exist", HttpStatus.NOT_FOUND, "name");

        }else if (gname.equalsIgnoreCase("manager") ||
                gname.equalsIgnoreCase("validUserAccounts")){

                String message = String.format("Group, (%s), can not be modified", gname);
                log.warn(message);
                throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "group");

        }else if (!gname.equalsIgnoreCase(group.getName()) && result != null ) {
            String message = String.format("Group, %s, already exists.", group.getName());
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "name");

        }else if(canEditGroup(gname)){
            group = repository.setMembersdnAndOwnersdn(group);

            if(gname.equals(group.getName())){
                group.setDn(temp.getDn());
                springGroupRepository.save(group);
            }else{
                springGroupRepository.save(group);
                springGroupRepository.delete(temp);
            }

        }else{
            throw new NotValidCustomException("User is not owner of group", HttpStatus.UNAUTHORIZED, "name");
        }

        return repository.findByName(group.getName());
    }

    @Override
    public Boolean delete(String gname) throws NotValidCustomException {
        log.trace("GROUP_SERVICE::Delete. $groupName: {}", gname);

        Group group = springGroupRepository.findByName(gname);

        if(group == null) {
            throw new NotValidCustomException(
                    String.format("Group, %s, does not exist.", gname),
                    HttpStatus.NOT_FOUND,
                    "group"
            );

        } else if (gname.equalsIgnoreCase("manager") || gname.equalsIgnoreCase("validUserAccounts")){
            String message = String.format("Group, (%s), can not be removed", gname);
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "group");

        }else if(canEditGroup(gname)){
            springGroupRepository.delete(group);

        } else {
            String message = String.format("Group, %s, can't be removed. Only owner can remote the group", gname);
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "group");
        }

        return true;
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
            throw new NotValidCustomException("Group name or username is missing.", HttpStatus.NOT_FOUND, "group");
        }

        if(canEditGroup(gname)){
            Group group = springGroupRepository.findByName(gname);
            Usuario member = springUserRepository.findByUsername(username);

            group.addMember(member);
            springGroupRepository.save(group);

            return repository.findByName(gname);

        }else{
            throw new NotValidCustomException(
                    String.format("Member, %s, can't be added to group, %s. User, %s, is not owner of the group", username, gname, getLoggedUser()),
                    HttpStatus.UNAUTHORIZED,
                    "group"
            );
        }
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
            if(canEditGroup(gname) || authUsername.equals(username)){
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
            throw new NotValidCustomException(e.getMessage(), HttpStatus.FORBIDDEN, "user");
        }

        return (result == null) ? null : repository.findByName(gname);
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
        Group group = repository.findByName(gname);
        Usuario user = springUserRepository.findByUsername(username);

        try {
            if (group == null || user == null) {
                throw new NotValidCustomException(
                        String.format("Failed to remove owner, %s, form group, %s", username, gname),
                        HttpStatus.NOT_FOUND,
                        "group"
                );
            }

            if (canEditGroup(gname)) {
                group.removeOwner(user);
                springGroupRepository.save(group);
            }else{
                throw new NotValidCustomException(
                        String.format("User, %s, is not authorized to EDIT group: %s", username, gname),
                        HttpStatus.UNAUTHORIZED,
                        "group"
                );
            }
        }catch(IOException e){
            throw new NotValidCustomException(e.getMessage(), HttpStatus.FORBIDDEN, "group");
        }

//        try {
//            Group group = getGroupOwner(gname);
//            Usuario user = getUser(username);
//            group.removeOwner(user);
//            grupo = springGroupRepository.save(group);
//        }catch (IOException e){
//            throw new NotValidCustomException(e.getMessage(), HttpStatus.BAD_REQUEST, "group");
//        }

        return repository.findByName(gname);
    }

    @Override
    public GroupList findAllByMember(Usuario user) {
        return repository.findAllByMember(user.getUsername());
    }

    @Override
    public GroupList testSpringLdapGroup(String username) {

        return repository.findAllByOwner(username);
    }

    @Override
    public Boolean isOwner(String groupname, String username) throws NotValidCustomException {
        Boolean result = false;

        if(canViewGroup(groupname)){
            result = repository.isOwner(groupname, username);
        }else{
            throw new NotValidCustomException(
                    String.format("User, %s, is not authorized to VIEW the group: %s", username, groupname),
                    HttpStatus.UNAUTHORIZED,
                    "port"
            );
        }

        return result;
    }

    private String getLoggedUser(){
        String result = request.getUserPrincipal().getName();
        log.trace("GROUP_SERVICE::getLoggedUser. $loginUsername: {}", result);
        return result;
    }

    private Boolean canEditGroup(String gname) {
        String loginUsername = getLoggedUser();
        Boolean result = false;

        if(admuser.equals(loginUsername)) {
            result = true;

        }else if(repository.isOwner(gname, loginUsername)){
            result = true;

        }
//        else{
//            throw new NotValidCustomException(
//                    String.format("User, %s, is no authorized to EDIT group, %s", loginUsername, gname),
//                    HttpStatus.UNAUTHORIZED,
//                    "group"
//            );
//        }

        return result;
    }

    private Boolean canViewGroup(String gname) {
        Boolean result = false;
        String loginUsername = getLoggedUser();

        if(canEditGroup(gname)){
            result = true;

        }else if(repository.isMember(gname, loginUsername)){
            result = true;
        }
//        else{
//            throw new NotValidCustomException(
//                    String.format("User, %s, is no authorized to VIEW group, %s", loginUsername, gname),
//                    HttpStatus.UNAUTHORIZED,
//                    "group"
//            );
//        }

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
