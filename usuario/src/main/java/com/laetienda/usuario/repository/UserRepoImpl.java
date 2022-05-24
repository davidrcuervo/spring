package com.laetienda.usuario.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import com.laetienda.usuario.lib.LdapDn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class UserRepoImpl implements UserRepository{
    final private static Logger log = LoggerFactory.getLogger(UserRepoImpl.class);

    @Autowired
    private UserLdapRepository repository;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LdapDn dn;

    /**
     *
     * @param username
     * @return user, or null if user does not exist
     */
    @Override
    public Usuario find(String username) {
        Usuario result = null;
        Name userdn = dn.getUserDn(username);

        try {
            result = repository.findById(userdn).get();
        }catch(NoSuchElementException e){
            log.warn("User does not exist. $dn: {}", userdn);
            log.debug(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public List<Usuario> findByEmail(String email) {
        List<Usuario> result = new ArrayList<>();

        try {
            LdapQuery ldapquery = query().base(dn.getUserDn()).where("objectclass").is("inetOrgPerson")
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
    public UsuarioList findAll() {
        log.debug("running find all, ldap user repository");
        UsuarioList result = new UsuarioList();

        repository.findAll(query()
                .base(dn.getUserDn())
                .where("objectclass").is("inetOrgPerson")
        ).forEach((user) -> {
            log.trace("username: {} | fullname: {}", user.getUsername(), user.getFullName());
            result.addUser(user);
        });

        return result;
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
        user.setDn(dn.getUserDn(user.getUsername()));

        user.setFullName(
                user.getFirstname() + " " +
                (user.getMiddlename() != null ? user.getMiddlename() + " " : "") +
                user.getLastname()
        );
        log.trace("fullName: {}", user.getFullName());
        log.trace("is new: {}", user.isNew());
        return repository.save(user);
    }

    @Override
    public void delete(Usuario user) throws NotValidCustomException {

        if(user != null) {
            user.setDn(dn.getUserDn(user.getUsername()));
            repository.delete(user);
        }else{
            NotValidCustomException ex = new NotValidCustomException("Failed to remove user", HttpStatus.BAD_REQUEST);
            ex.addError("username", "Not user found with that username.");
            throw ex;
        }
    }

    @Override
    public boolean authenticate(Usuario user) {

        boolean result = ldapTemplate.authenticate(
                dn.getUserDn(user.getUsername()).toString(),
                String.format("uid=%s", user.getUsername()),
                user.getPassword());

        return result;
    }
}
