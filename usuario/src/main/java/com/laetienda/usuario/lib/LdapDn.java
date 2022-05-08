package com.laetienda.usuario.lib;

import org.springframework.stereotype.Component;

import javax.naming.Name;

@Component
public interface LdapDn {
    Name getUserDn(String uid);

    Name getGroupDn(String cn);

    Name group();
    public Name getCompleteGroupDn();
    public Name getCompleteGoupDn(String groupname);

    public Name getCompleteUserDn(String username);
}
