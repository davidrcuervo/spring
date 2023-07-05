package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringUserRepository extends LdapRepository<Usuario> {

    Usuario findByToken(String token);
    Usuario findByUsername(String username);
    List<Usuario> findByEmail(String email);
}
