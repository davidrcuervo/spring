package com.laetienda.usuario.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.InvalidNameException;
import javax.naming.Name;

public class LdapDnImplB implements LdapDn {
    private final static Logger log = LoggerFactory.getLogger(LdapDnImplB.class);

    @Value("${spring.ldap.base}")
    private String dndomain;

//    @Value("${ldap.dn.group}")
//    private String groupdn;

//    @Value("${ldap.dn.people}")
//    private String peopledn;

    @Value("${spring.ldap.base}")
    private String base;

//    @Override
//    public Name getUserDn(String uid) {
//        return LdapNameBuilder.newInstance(peopledn).add("uid", uid).build();
//    }

//    @Override
//    public Name getUserDn() {
//        return LdapNameBuilder.newInstance(peopledn).build();
//    }

//    @Override
//    public Name getDomainDn() {
//        return LdapNameBuilder.newInstance(dndomain).build();
//    }

    @Override
    public String getUsername(Name dn) {
        if(dn.size() < 0){
            log.trace("USER_LIB_DN::getUserName -> Size of dn should not be empty");
            return null;
        }else if(dn.get(dn.size() -1) == null || dn.get(dn.size() -1).isBlank()) {
            log.trace("USER_LIB_DN::getUserName -> Size of dn should not be empty");
            return null;
        }else if(!("ou=people".equals(dn.get(dn.size()-2)))){
            log.trace("USER_LIB_DN::getUserName -> name dn is not for user");
            return null;
        }else if(!("uid".equals(dn.get(dn.size()-1).split("=")[0]))){
            log.trace("USER_LIB_DN::getUserName -> name dn is not for user");
            return null;
        }else {
            return dn.get(dn.size() - 1).split("=")[1];
        }
    }

//    @Override
//    public Name getGroupDn(String cn) {
//        return LdapNameBuilder.newInstance(groupdn).add("cn", cn).build();
//    }

    @Override
    public Name getFullDn(Name dn) {
        Name result = dn;
        try {
            result = LdapNameBuilder.newInstance(base).build().addAll(dn);
        } catch (InvalidNameException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return result;
    }

//    @Override
//    public Name removeBase(Name dn) {
//        try {
//            Name baseDn = LdapNameBuilder.newInstance(base).build();
//
//            for(int c=0; c < baseDn.size(); c++){
//                dn.remove(0);
//            }
//
//            for(int c=0; c < dn.size(); c++){
//                log.trace("DN::removeBase. $dn.get({}): {}", c, dn.get(c));
//            }
//        } catch (InvalidNameException e) {
//            log.error(e.getMessage());
//            log.debug(e.getMessage(), e);
//        }
//        return dn;
//    }

//    @Override
//    public Name getGroupDn() {
//        return LdapNameBuilder.newInstance(groupdn).build();
//    }

//    @Override
//    public Name getCompleteGroupDn() {
//        return getGroupDn();
//    }

//    @Override
//    public Name getCompleteGoupDn(String groupname) {
//        return getGroupDn(groupname);
//    }

//    @Override
//    public Name getCompleteUserDn(String username) {
//        return getUserDn(username);
//    }
}
