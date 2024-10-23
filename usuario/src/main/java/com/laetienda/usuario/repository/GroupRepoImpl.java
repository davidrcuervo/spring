package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.lib.LdapDn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.InvalidNameException;
import javax.naming.Name;

import java.util.*;

public class GroupRepoImpl implements GroupRepository{
    private final static Logger log = LoggerFactory.getLogger(GroupRepoImpl.class);

    @Autowired
    private SpringGroupRepository springRepository;

    @Autowired
    private SpringUserRepository springUserRepository;

    @Autowired
    private LdapDn dn;
//
//    @Value("${spring.ldap.base}")
//    private String ldapBase;



    @Override
    public GroupList findAll() {
        GroupList result = new GroupList();

        springRepository.findAll().forEach(group -> {
            group = findOwners(group);
            group = findMembers(group);
            result.addGroup(group);
        });

        return result;
    }

    public Group findByName(String gname){
        Group result = springRepository.findByName(gname);

        if(result != null) {
            result = findMembers(result);
            result = findOwners(result);
        }
        return result;
    }

    private Group findOwners(Group group) {
        Map<String, Usuario> owners = new HashMap<>();
        log.trace("# of owners: {}", group.getOwnersdn().size());
        group.getOwnersdn().forEach(
                (ownerdn) -> {
                    Usuario temp = springUserRepository.findById(ownerdn).get();
//                    String username = ownerdn.split(",")[0].split("=")[1];
                    owners.put(temp.getUsername(), temp);
                });
        group.setOwners(owners);
        return group;
    }

    private Group findMembers(Group result) {
        Map<String, Usuario> members = new HashMap<>();
//        log.trace("GROUP_REPOSITORY::findMembers# of members: {}", result.getMembersdn().size());
        result.getMembersdn().forEach(
                (memberdn) -> {
//                    log.trace("GROUP_REPOSITORY::findMembers. $memberdn: {}", memberdn);
                    Usuario member = springUserRepository.findById(memberdn).get();
                    members.put(member.getUsername(), member);
                });
        result.setMembers(members);
        return result;
    }

    @Override
    public GroupList findAllByOwner(String username) {
        GroupList result = new GroupList();
        Usuario owner = springUserRepository.findByUsername(username);

        springRepository.findByOwnersdn(owner.getId()).forEach(group -> {
            group = findOwners(group);
            group = findMembers(group);
            result.addGroup(group);
        });

        log.trace("# of groups: {}", result.getGroups().size());
        return result;
    }

    @Override
    public GroupList findByMemberAndMember(String username1, String username2) {
        GroupList result = new GroupList();
        Name userdn1 = springUserRepository.findByUsername(username1).getId();
        Name userdn2 = springUserRepository.findByUsername(username2).getId();

        springRepository.findByMembersdnAndMembersdn(userdn1,userdn2).forEach(group -> {
            group = findOwners(group);
            group = findMembers(group);
            result.addGroup(group);
        });

        return result;
    }

    @Override
    public Group setMembersdnAndOwnersdn(Group group) {

        if(group.getOwners() != null) {
            group.getOwners().forEach(
                    (key, owner) -> {
                        Usuario ownerLdap = springUserRepository.findByUsername(key);
                        group.addOwner(ownerLdap);
                    });
        }

        if(group.getMembers() != null) {
            log.trace("GROUP_REPOSITORY::setMembersdnAndOwnersdn. $group.members.size: {}", group.getMembers().size());
            group.getMembers().forEach(
                    (key, member) -> {
                        Usuario memberLdap = springUserRepository.findByUsername(key);
                        group.addMember(memberLdap);
                    });
        }

        return group;
    }

    @Override
    public boolean isOwner(String gName, String username) {
        Usuario user = springUserRepository.findByUsername(username);
        List<Group> groups = springRepository.findByNameAndOwnersdn(gName, user.getId());

        log.trace("GROUP::Repository. User, {}, is owner of {} groups.", username, groups.size());
        for(Group g : groups){
            log.trace("GROUP::Repository. User, {}, is owner of {}.", username, g.getName());
        }

        return groups.size() > 0 ? true : false;
    }

    @Override
    public boolean isMember(String gName, String username) {
        Usuario member = springUserRepository.findByUsername(username);
        List<Group> groups = springRepository.findByNameAndMembersdn(gName, member.getId());
        log.trace("GROUP::Repo. $groups.size: {}", groups.size());
        return groups.size() > 0 ? true: false;
    }

    @Override
    public GroupList findAllByMember(String username) {
        GroupList result = new GroupList();

            Usuario member = springUserRepository.findByUsername(username);
            Name memberdn = member.getId();

            log.trace("GROUP_REPOSITORY::findAllByMember. $memberdn: {}", memberdn);

            List<Group> groups = springRepository.findByMembersdn(memberdn);
            log.trace("GROUP_REPOSITORY::findAllByMember. $groups.size: {}", groups.size());

            springRepository.findByMembersdn(memberdn).forEach(group -> {
                log.trace("GROUP::REPOSITORY::findAllByMember. $groupname: {}", group.getName());
                group = findMembers(group);
                group = findOwners(group);
                result.addGroup(group);
            });

        return result;
    }
}
