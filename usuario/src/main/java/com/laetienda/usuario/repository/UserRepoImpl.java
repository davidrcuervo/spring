package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.service.UserLdapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class UserRepoImpl implements UserRepository{
    final private static Logger log = LoggerFactory.getLogger(UserRepoImpl.class);

    @Autowired
    private UserLdapRepository repository;

    @Value("${ldap.people.dn}")
    private String people_dn;

    @Override
    public Usuario find(String username) {
        Usuario result = null;
        Name dn = LdapNameBuilder.newInstance().add("ou", "people").add("uid", username).build();

        try {
            result = repository.findById(dn).get();
        }catch(NoSuchElementException e){
            log.warn("User does not exist. $dn: {}", dn);
            log.debug(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<Usuario> findByEmail(String email) {
        List<Usuario> result = new ArrayList<Usuario>();

        try {
            LdapQuery ldapquery = query().where("objectclass").is("inetOrgPerson")
                    .and("mail").is(email);
            repository.findAll(ldapquery).forEach(result::add);
            log.trace("Users found with email {}: {}", email, result.size());
        }catch(NoSuchElementException e){
            log.warn("email not found. $email: {}", email);
            log.debug(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<Usuario> findAll() {
        return repository.findAll();
    }

    @Override
    public Usuario create(Usuario user) {
        Name dn = LdapNameBuilder.newInstance().add("ou", "people").add("uid", user.getUsername()).build();
//        Name dn = LdapUtils.emptyLdapName();
        user.setDn(dn);
        user.setNew(true);
        user.setFullName(
                user.getFirstname() + " " +
                user.getMiddlename() + " " +
                user.getLastname()
        );
        log.trace("is new: {}", user.isNew());
        repository.save(user);
        return user;
    }

    @Override
    public Usuario edit(Usuario user) {
        return user;
    }

    @Override
    public void delete(Usuario user) {

    }


}
