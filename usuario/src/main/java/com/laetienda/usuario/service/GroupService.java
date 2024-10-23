package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Service;

@Service
public interface GroupService {

    /**
     * Return group if user is member, owner or manager
     * @param name Name of the group
     * @return
     * @throws NotValidCustomException NotFound if group does not exist, or Unauthorized if user does not belong to group
     */
    Group findByName(String name) throws NotValidCustomException;

    /**
     * Find all groups if it is manager, or groups where user is member
     * @return GroupList, getGroups().size() is 0 when there is no groups. Value will not be null
     */
    GroupList findAll() throws NotValidCustomException;

    /**
     * Return list of Groups where member belongs to.
     * @param username
     * @return List of Groups
     * @throws NotValidCustomException When user is not manager or user does not belong to group
     */
    GroupList findAllByMember(String username) throws NotValidCustomException;

    Group create(Group group) throws NotValidCustomException;
    Group update(Group group, String gname) throws NotValidCustomException;
    Boolean delete(String gname) throws NotValidCustomException;
    Boolean isMember(String gname, String username) throws NotValidCustomException;
    Group addMember(String gname, String username) throws NotValidCustomException;
    Group removeMember(String gname, String username) throws NotValidCustomException;
    Group addOwner(String gname, String username) throws NotValidCustomException;
    Group removeOwner(String gname, String username) throws NotValidCustomException;

    GroupList findAllByMember(Usuario user);

    GroupList testSpringLdapGroup(String gname);

    Boolean isOwner(String groupname, String username) throws NotValidCustomException;
//    Group addMemberToValidUserAccounts(String username) throws NotValidCustomException;
}
