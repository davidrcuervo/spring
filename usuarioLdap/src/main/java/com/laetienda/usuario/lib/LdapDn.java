package com.laetienda.usuario.lib;

import org.springframework.stereotype.Component;

import javax.naming.Name;

public interface LdapDn {
//    Name getUserDn(String uid);
//    public Name getUserDn();
//    public Name getDomainDn();
    String getUsername(Name dn);

//    Name getGroupDn(String cn);
    Name getFullDn(Name dn);
//    Name removeBase(Name dn);

//    Name getGroupDn();
//    public Name getCompleteGroupDn();
//    public Name getCompleteGoupDn(String groupname);

//    public Name getCompleteUserDn(String username);
}
