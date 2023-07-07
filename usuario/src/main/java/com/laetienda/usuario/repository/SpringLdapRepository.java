package com.laetienda.usuario.repository;

import com.laetienda.model.user.Group;
import org.springframework.data.ldap.repository.LdapRepository;

public interface SpringLdapRepository extends LdapRepository<Group> {

}
