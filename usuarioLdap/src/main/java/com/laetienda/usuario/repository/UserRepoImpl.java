package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import com.laetienda.usuario.lib.LdapDn;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import javax.naming.Name;
import javax.naming.directory.*;
import java.util.Optional;

public class UserRepoImpl implements UserRepository{
    final private static Logger log = LoggerFactory.getLogger(UserRepoImpl.class);

    @Autowired private SpringUserRepository springUserRepository;
    @Autowired private LdapTemplate ldapTemplate;
    @Autowired private Environment env;
//    @Autowired private StringEncryptor jasypte;
    @Autowired private LdapDn dn;

    @Override
    public Usuario findbyUsername(String username) {
        log.trace("USER_REPOSITORY::findByUsername. $username {}", username);
        Usuario result = springUserRepository.findByUsername(username);
//        if(result != null) {
//            findById(dn.getFullDn(result.getId()));
//        }
        return result;
    }

    @Override
    public Usuario findById(Name id) {
        log.trace("USER_REPOSITORY::findById. $fullDn: {}", id);
//        log.trace("USER_REPOSITORY::findById. $simpleDn: {}", dn.removeBase(id));
//        Optional<Usuario> result = springUserRepository.findById(id);
////        Usuario user = getLdapTemplate().findByDn(id, Usuario.class);
//        Usuario user = springUserRepository.findById(id).get();"USER_REPOSITORY::findById.
//        log.trace("USER_REPOSITORY::findById. $username {}", user.getUsername());
//        log.trace("USER_REPOSITORY::findById. $fullname {}", user.getFullName());
        return findbyUsername(dn.getUsername(id));
    }

    @Override
    public UsuarioList findAll() {
        log.debug("running find all, ldap user repository");
        UsuarioList result = new UsuarioList();

        springUserRepository.findAll().forEach((user) -> {
                    log.trace("username: {} | fullname: {}", user.getUsername(), user.getFullName());
                    result.addUser(user);
                });

        return result;
    }

    @Override
    public Usuario modifyAtrribute(String username, String attribute, String value, int action) {
        Name userDn = springUserRepository.findByUsername(username).getId() ;
        Attribute attr = new BasicAttribute(attribute, value);
        ModificationItem tokenItem = new ModificationItem(action, attr);
        ldapTemplate.modifyAttributes(userDn, new ModificationItem[]{tokenItem});
        return springUserRepository.findByUsername(username);
    }

    @Override
    public boolean authenticate(Usuario user) {
        Name userdn = springUserRepository.findByUsername(user.getUsername()).getId();
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

//    private LdapTemplate getLdapTemplate() {
//
//        LdapContextSource contextSource = new LdapContextSource();
//        contextSource.setUrl(env.getProperty("spring.ldap.urls"));
////        contextSource.setBase(env.getProperty("spring.ldap.base"));
//        contextSource.setUserDn(env.getProperty("spring.ldap.username"));
//        contextSource.setPassword(env.getProperty("spring.ldap.password"));
//        contextSource.afterPropertiesSet();
//        LdapTemplate ldapTemplate1 = new LdapTemplate(contextSource);
//
//        try {
//            ldapTemplate1.afterPropertiesSet();
//            return ldapTemplate1;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            log.debug(e.getMessage(), e);
//            throw new RuntimeException(e);
////            throw new NotValidCustomException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "user");
//        }
//    }
}
