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

import javax.naming.Name;
import javax.naming.directory.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public class UserRepoImpl implements UserRepository{
    final private static Logger log = LoggerFactory.getLogger(UserRepoImpl.class);

    @Autowired
    private SpringUserRepository repository;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private LdapDn dn;

    @Override
    public UsuarioList findAll() {
        log.debug("running find all, ldap user repository");
        UsuarioList result = new UsuarioList();

        repository.findAll().forEach((user) -> {
                    log.trace("username: {} | fullname: {}", user.getUsername(), user.getFullName());
                    result.addUser(user);
                });

//        repository.findAll(query()
//                .base(dn.getUserDn())
//                .where("objectclass").is("inetOrgPerson")
//        ).forEach((user) -> {
//            log.trace("username: {} | fullname: {}", user.getUsername(), user.getFullName());
//            result.addUser(user);
//        });

        return result;
    }

//    @Override
//    public Usuario create(Usuario user) {
//        Name userDn = dn.getUserDn(user.getUsername());
//        log.trace("new user dn: {}", userDn);
//        ldapTemplate.bind(userDn, null, buildAttributes(user));
//        Usuario result = repository.findByUsername(user.getUsername());
//        return result;
//    }

//    @Override
//    public Usuario update(Usuario user) {
//        Usuario storedUser = repository.findByUsername(user.getUsername());
//
//        if(!user.getFirstname().equals(storedUser.getFirstname())){
//
//        }
//
//        if(!user.getMiddlename().equals(storedUser.getMiddlename())){
//
//        }
//
//        if(!user.getLastname().equals(storedUser.getLastname())){
//
//        }
//
//        if(!user.getEmail().equals(storedUser.getEmail())){
//
//        }
//
//
//        return repository.findByUsername(user.getUsername());
////        repository.save(user);
////        user.setNew(false);
////        return save(user);
//    }

//    private Attributes buildAttributes(Usuario user){
//        BasicAttribute ocattr = new BasicAttribute("objectclass");
//        ocattr.add("top");
//        ocattr.add("person");
//        ocattr.add("inetOrgPerson");
//        Attributes attrs = new BasicAttributes();
//        attrs.put(ocattr);
//        attrs.put("cn", user.getUsername());
//
////        user.setFullName(
////                user.getFirstname() + " " +
////                        (user.getMiddlename() != null ? user.getMiddlename() + " " : "") +
////                        user.getLastname()
////        );
////
////        attrs.put("cn", user.getFullName());
//        attrs.put("givenName", user.getFirstname());
//
//        if(user.getMiddlename() != null && !user.getMiddlename().isBlank()) {
//            attrs.put("displayName", user.getMiddlename());
//        }
//
//        attrs.put("sn", user.getLastname());
//        attrs.put("mail", user.getEmail());
//        attrs.put("userPassword", user.getPassword().getBytes(StandardCharsets.UTF_8));
//        attrs.put("labeledURI", user.getToken());
//        return attrs;
//    }

    @Override
    public Usuario modifyAtrribute(String username, String attribute, String value, int action) {
        Name userDn = dn.getUserDn(username);
        Attribute attr = new BasicAttribute(attribute, value);
        ModificationItem tokenItem = new ModificationItem(action, attr);
        ldapTemplate.modifyAttributes(userDn, new ModificationItem[]{tokenItem});
        return repository.findByUsername(username);
    }
//
//    private Usuario deleteAttribute(String username, String attribute, String value){
//
//        Name userDn = dn.getUserDn(username);
//
//        return repository.findByUsername(username);
//    }

//    private Usuario save(Usuario user){
//        user.setDnTest(dn.getUserDn(user.getUsername()));
//
//        user.setFullName(
//                user.getFirstname() + " " +
//                (user.getMiddlename() != null ? user.getMiddlename() + " " : "") +
//                user.getLastname()
//        );
//        log.trace("dn: {}", user.getDn());
//        log.trace("fullName: {}", user.getFullName());
//        log.trace("is new: {}", user.isNew());
//
//        return repository.save(user);
//    }

//    @Override
//    public void delete(Usuario user) throws NotValidCustomException {
//
//        if(user != null) {
////            user.setDnTest(dn.getUserDn(user.getUsername()));
//            repository.delete(user);
//        }else{
//            NotValidCustomException ex = new NotValidCustomException("Failed to remove user", HttpStatus.BAD_REQUEST);
//            ex.addError("username", "Not user found with that username.");
//            throw ex;
//        }
//    }

    @Override
    public boolean authenticate(Usuario user) {
        Name userdn = dn.getUserDn(user.getUsername());
        log.trace("Authenticating user: {}", userdn);
        boolean result = false;

        try {
            result = ldapTemplate.authenticate(
                    userdn.toString(),
                    String.format("uid=%s", user.getUsername()),
                    user.getPassword());
            log.trace("Authentication result. $result: {}", result ? "true" : "false");
        }catch (Exception e){
            log.info("Failed to authenticate user: {}, message: {}", user.getUsername(), e.getMessage());
            log.debug(e.getMessage(), e);
            result = false;
        }

        return result;
    }

//    @Override
//    public Usuario findByToken(String token){
//        log.debug("Find user by token. $token: {}", token);
//        Usuario result = null;
//        List<Usuario> temp = new ArrayList<>();
//
//        try {
//            LdapQuery ldapquery = query().base(dn.getUserDn()).where("objectclass").is("inetOrgPerson")
//                    .and("labeledURI").is(token);
//
//            repository.findAll(ldapquery).forEach(temp::add);
//            log.trace("lenght of users with token. $lenght: {}", temp.size());
//
//            if(temp.size() == 0) {
//                result = null;
//            }else if(temp.size() == 1){
//                result = temp.get(0);
//                log.trace("Users found with token {}: {}", token, result.getUsername());
//            }else{
//                throw new IOException("There is more than one user with same token");
//            }
//
//        }catch(NoSuchElementException e){
//            log.warn("User token not found. $: {}", token);
////            log.debug(e.getMessage(), e);
//        }catch(IOException e){
//            log.error("There is more than one user with same token. $token {}", token);
//            log.debug(e.getMessage(), e);
//        }
//
//        return result;
//    }
}
