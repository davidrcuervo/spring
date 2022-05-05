package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserLdapRepository extends LdapRepository<Usuario> {

}
