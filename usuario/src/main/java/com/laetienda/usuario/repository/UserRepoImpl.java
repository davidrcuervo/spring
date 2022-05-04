package com.laetienda.usuario.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.service.UserLdapRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
        user.setNew(true);
        return save(user);
    }

    @Override
    public Usuario update(Usuario user) {
        user.setNew(false);
        return save(user);
    }

    private Usuario save(Usuario user){
        user.setDn(dn(user));

        user.setFullName(
                user.getFirstname() + " " +
                (user.getMiddlename() != null ? user.getMiddlename() + " " : "") +
                user.getLastname()
        );
        log.trace("is new: {}", user.isNew());
        return repository.save(user);
    }


    @Override
    public void delete(Usuario user) throws NotValidCustomException {

        if(user != null) {
            user.setDn(dn(user));
            repository.delete(user);
        }else{
            NotValidCustomException ex = new NotValidCustomException("Failed to remove user", HttpStatus.BAD_REQUEST);
            ex.addError("username", "Not user found with that username.");
            throw ex;
        }
    }

    private Name dn(Usuario user){
        return LdapNameBuilder.newInstance().add("ou", "people").add("uid", user.getUsername()).build();
    }
}
