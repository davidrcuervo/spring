package com.laetienda.usuario.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Component;

import javax.naming.Name;

public class LdapDnImpl implements LdapDn {
    final static private Logger log = LoggerFactory.getLogger(LdapDnImpl.class);

    @Value("${ldap.dn.people}")
    private String peopledn;

    @Value("${ldap.dn.group}")
    private String groupdn;

    @Value("${spring.ldap.base}")
    private String base;

    @Override
    public Name getUserDn(String uid){
        return LdapNameBuilder.newInstance(peopledn).add("uid", uid).build();
    }

    @Override
    public Name getGroupDn(String cn){
        return LdapNameBuilder.newInstance(groupdn).add("cn", cn).build();
    }

    @Override
    public Name group(){
        return LdapNameBuilder.newInstance(groupdn).build();
    }

    @Override
    public Name getCompleteGroupDn() {
        return LdapNameBuilder.newInstance(base).add(groupdn).build();
    }

    @Override
    public Name getCompleteGoupDn(String groupname){
        return LdapNameBuilder.newInstance(base).add(groupdn).add("cn", groupname).build();
    }

    @Override
    public Name getCompleteUserDn(String username) {
        return LdapNameBuilder.newInstance(base).add(peopledn).add("uid", username).build();
    }

    public static void main (String[] args){
        Name dn = LdapNameBuilder.newInstance("ou=people,dc=la-etienda,dc=com").add("uid", "admuser").build();
        log.info("$dn: {}", dn);
        log.info("{}", dn.get(3).split("=")[1]);
    }
}
