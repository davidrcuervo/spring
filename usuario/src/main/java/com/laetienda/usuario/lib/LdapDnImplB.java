package com.laetienda.usuario.lib;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;

public class LdapDnImplB implements LdapDn {

    @Value("${ldap.dn.domain}")
    private String dndomain;

    @Value("${ldap.dn.group}")
    private String groupdn;

    @Value("${ldap.dn.people}")
    private String peopledn;
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
