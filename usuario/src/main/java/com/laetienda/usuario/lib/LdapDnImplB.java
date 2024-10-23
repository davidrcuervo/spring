package com.laetienda.usuario.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.InvalidNameException;
import javax.naming.Name;

public class LdapDnImplB implements LdapDn {
    private final static Logger log = LoggerFactory.getLogger(LdapDnImplB.class);

    @Value("${ldap.dn.domain}")
    private String dndomain;

    @Value("${ldap.dn.group}")
    private String groupdn;

    @Value("${ldap.dn.people}")
    private String peopledn;

    @Value("${spring.ldap.base}")
    private String base;

    @Override
    public Name getUserDn(String uid) {
        return LdapNameBuilder.newInstance(peopledn).add("uid", uid).build();
    }

    @Override
    public Name getUserDn() {
        return LdapNameBuilder.newInstance(peopledn).build();
    }

    @Override
    public Name getDomainDn() {
        return LdapNameBuilder.newInstance(dndomain).build();
    }

    @Override
    public Name getGroupDn(String cn) {
        return LdapNameBuilder.newInstance(groupdn).add("cn", cn).build();
    }

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

    @Override
    public Name getGroupDn() {
        return LdapNameBuilder.newInstance(groupdn).build();
    }

    @Override
    public Name getCompleteGroupDn() {
        return getGroupDn();
    }

    @Override
    public Name getCompleteGoupDn(String groupname) {
        return getGroupDn(groupname);
    }

    @Override
    public Name getCompleteUserDn(String username) {
        return getUserDn(username);
    }
}
