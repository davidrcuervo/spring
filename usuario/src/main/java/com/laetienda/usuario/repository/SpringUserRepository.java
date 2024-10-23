package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;
import javax.naming.Name;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpringUserRepository extends LdapRepository<Usuario> {

    Optional<Usuario> findById(Name dn);
    Usuario findByToken(String token);
    Usuario findByUsername(String username);
    List<Usuario> findByEmail(String email);
//    List<Usuario> findAll();
}
