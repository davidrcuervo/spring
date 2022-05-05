package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.lib.LdapDnImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;

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
    public Group create(Group group) {
        return null;
    }

    @Override
    public Group findByName(String name) {

        Group result = repository.findById(dn.getGroupDn(name)).get();
        result = this.findMembers(result);
        result = this.findOwners(result);
        return result;

    }

    private Group findMembers(Group result) {
        List<Usuario> members = new ArrayList<Usuario>();
        log.trace("# of members: {}", result.getMembersdn().size());
        result.getMembersdn().forEach(
                (memberdn) -> {
                    String username = memberdn.get(3).split("=")[1];
                    members.add(userRepo.find(username));
                });
        result.setMembers(members);
        return result;
    }

    private Group findOwners(Group result) {
        List<Usuario> owners = new ArrayList<>();
        log.trace("# of owners: {}", result.getOwnersdn().size());
        result.getOwnersdn().forEach(
                (ownerdn) -> {
                    String username = ownerdn.get(3).split("=")[1];
                    owners.add(userRepo.find(username));
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
                .base(dn.group())
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
    public Group update(String name, Group group) {
        return null;
    }

    @Override
    public Group delete(Group group) {
        return null;
    }

    @Override
    public boolean isOwner(Group group, String username) {
        return group.getOwnersdn().contains(dn.getUserDn(username));
    }

    @Override
    public boolean isMember(Group group, String username) {
        return group.getMembers().contains(dn.getUserDn(username));
    }

}
