package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.lib.LdapDn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.naming.Name;
import java.io.IOException;
import java.util.*;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class GroupRepoImpl implements GroupRepository{
    private final static Logger log = LoggerFactory.getLogger(GroupRepoImpl.class);

    @Autowired
    private GroupLdapRepository repository;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private LdapDn dn;

    @Override
    public Group create(Group group, String ownerusername) {
        Usuario owner = userRepo.find(ownerusername);
        owner.setDn(dn.getCompleteUserDn(ownerusername));
        log.trace("1. $owner dn: {}", dn.getCompleteUserDn(ownerusername).toString());
        log.trace("2. $owner dn: {}", owner.getDn().toString());
        group.setDn(dn.getGroupDn(group.getName()));
        group.setNew(true);
        group.addOwner(owner);

        group.getOwners().forEach((username, ownuser) -> {
            ownuser.setDn(dn.getCompleteUserDn(username));
            group.addOwner(ownuser);
        });

        group.getMembers().forEach((username, memuser) -> {
            memuser.setDn(dn.getCompleteUserDn(username));
            group.addMember(memuser);
        });

        group.getMembersdn().forEach((memberdn) -> {
            log.trace("4. $member dn: {}", memberdn);
        });

        return repository.save(group);
    }

    /**
     *
     * @param name
     * @return group, null if group name does not exist
     */
    @Override
    public Group findByName(String name) {
        log.trace("Group dn: {}", dn.getGroupDn(name));
        Group result = null;
        Optional<Group> temp = repository.findById(dn.getGroupDn(name));

        if(temp.isEmpty()){
            log.debug("Group not found. $name: {}", name);
        }else {
            result = temp.get();
            result = this.findMembers(result);
            result = this.findOwners(result);
            return result;
        }

        return result;
    }

    private Group findMembers(Group result) {
        Map<String, Usuario> members = new HashMap<>();
        log.trace("# of members: {}", result.getMembersdn().size());
        result.getMembersdn().forEach(
                (memberdn) -> {
                    String username = memberdn.split(",")[0].split("=")[1];
                    members.put(username, userRepo.find(username));
                });
        result.setMembers(members);
        return result;
    }

    private Group findOwners(Group result) {
        Map<String, Usuario> owners = new HashMap<>();
        log.trace("# of owners: {}", result.getOwnersdn().size());
        result.getOwnersdn().forEach(
                (ownerdn) -> {
                    String username = ownerdn.split(",")[0].split("=")[1];
                    owners.put(username, userRepo.find(username));
                });
        result.setOwners(owners);
        return result;
    }

    @Override
    public List<Group> findAll(Usuario owner) {
        List<Group> result = new ArrayList<Group>();
        String ownerdn = dn.getCompleteUserDn(owner.getUsername()).toString();
//        log.trace("ownerdn: {}", ownerdn);
//        log.trace("groupdn: {}", dn.getCompleteGroupDn().toString());
        repository.findAll(query()
                .base(dn.getGroupDn())
                .where("objectclass").is("groupOfUniqueNames")
                .and("owner").is(ownerdn)
         ).forEach(result::add);

        log.trace("# of groups: {}", result.size());
        result.forEach(
                (group) -> {
                    this.findOwners(group);
                    this.findMembers(group);
                });

        return result;
    }

    @Override
    public List<Group> findAll(String username) {
        Usuario owner = userRepo.find(username);
        return findAll(owner);
    }

    @Override
    public Group update(Group group, String gname, String username) {
        Group result = null;

        Name olddn = dn.getGroupDn(gname);
        Name newdn = dn.getGroupDn(group.getName());

        if(gname.equals(group.getName())){
            group.setDn(dn.getGroupDn(group.getName()));
            repository.save(group);
            result = findByName(group.getName());
        }else{
            Group temp = repository.findById(olddn).get();
            temp.setName(group.getName());
            temp.setDn(newdn);
            create(temp, username);
            temp = repository.findById(olddn).get();
            repository.delete(temp);
            result = update(group, group.getName(), username);
        }

        return result;
    }

    @Override
    public Group delete(Group group) {
        Group result = group;
        group.setDn(dn.getGroupDn(group.getName()));
        repository.delete(group);

        return null;
    }

    @Override
    public boolean isOwner(Group group, String username) {
        Name gdn = dn.getGroupDn(group.getName());
        Name udn = dn.getUserDn(username);
        return repository.findById(gdn).get().getOwnersdn().contains(udn.toString());
    }

    @Override
    public boolean isMember(Group group, String username) {
        Name gdn = dn.getGroupDn(group.getName());
        Name udn = dn.getUserDn(username);
        return repository.findById(gdn).get().getMembersdn().contains(udn.toString());
    }

    @Override
    public Group addMember(Group group, Usuario user) {
        user.setDn(dn.getUserDn(user.getUsername()));
        group.setDn(dn.getGroupDn(group.getName()));
        group.addMember(user);
        repository.save(group);
        return findByName(group.getName());
    }

    @Override
    public Group removeMember(Group group, Usuario user) throws IOException {
        user.setDn(dn.getUserDn(user.getUsername()));
        group.setDn(dn.getGroupDn(group.getName()));
        group.removeMember(user);

        repository.save(group);
        return findByName(group.getName());
    }

    @Override
    public Group addOwner(Group group, Usuario user) {
        user.setDn(dn.getUserDn(user.getUsername()));
        group.setDn(dn.getGroupDn(group.getName()));

        group.addOwner(user);
        repository.save(group);
        return findByName(group.getName());
    }

    @Override
    public Group removeOwner(Group group, Usuario user) throws IOException {
        user.setDn(dn.getUserDn(user.getUsername()));
        group.setDn(dn.getGroupDn(group.getName()));

        group.removeOwner(user);
        repository.save(group);
        return findByName(group.getName());
    }
}
