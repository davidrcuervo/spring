package com.laetienda.usuario.lib;

import org.springframework.stereotype.Component;

import javax.naming.Name;

@Component
public interface LdapDn {
    Name getUserDn(String uid);
    public Name getUserDn();
    public Name getDomainDn();

    Name getGroupDn(String cn);

    Name getGroupDn();
    public Name getCompleteGroupDn();
    public Name getCompleteGoupDn(String groupname);

    public Name getCompleteUserDn(String username);
}
