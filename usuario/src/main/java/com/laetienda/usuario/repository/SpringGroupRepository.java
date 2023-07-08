package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import org.springframework.data.ldap.repository.LdapRepository;

import javax.naming.Name;
import java.util.List;

public interface SpringGroupRepository extends LdapRepository<Group> {

    Group findByName(String gName);

    List<Group> findByNameAndOwnersdn(String name, String ownerdn);

    List<Group> findByNameAndMembersdn(String name, String memberdn);

    List<Group> findByOwnersdn(String ownerdn);

    List<Group> findByMembersdn(String string);

    List<Group> findByMembersdnAndMembersdn(String userdn1, String userdn2);
}
