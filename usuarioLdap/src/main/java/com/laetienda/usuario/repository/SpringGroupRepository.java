package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import org.springframework.data.ldap.repository.LdapRepository;

import javax.naming.Name;
import java.util.List;

public interface SpringGroupRepository extends LdapRepository<Group> {

    Group findByName(String groupname);

    List<Group> findByNameAndOwnersdn(String name, Name ownerdn);

    List<Group> findByNameAndMembersdn(String name, Name memberdn);

    List<Group> findByOwnersdn(Name ownerdn);

    List<Group> findByMembersdn(Name memberdn);

    List<Group> findByMembersdnAndMembersdn(Name userdn1, Name userdn2);
}
