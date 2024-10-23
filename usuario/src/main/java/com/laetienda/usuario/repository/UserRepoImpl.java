package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.Name;
import javax.naming.directory.*;

public class UserRepoImpl implements UserRepository{
    final private static Logger log = LoggerFactory.getLogger(UserRepoImpl.class);

    @Autowired
    private SpringUserRepository springUserRepository;

    @Autowired
    private LdapTemplate ldapTemplate;

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
}
