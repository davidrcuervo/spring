package com.laetienda.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;
import java.util.List;
import java.util.Set;

@Entry(objectClasses = {"groupOfUniqueNames"}, base = "ou=wroups")
final public class Group implements Persistable {
    
    @Id
    @JsonIgnore
    private Name dn;
    
    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 1)
    private String name;

    @Attribute(name = "owner")
    @JsonIgnore
    private Set<Name> ownersdn;

    @Transient
    private List<Usuario> owners;

    @Attribute(name = "uniqueMember")
    @JsonIgnore
    private Set<Name> membersdn;

    @Transient
    private List<Usuario> members;

    @Transient
    private boolean newFlag;

    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Set<Name> getMembersdn() {
        return membersdn;
    }

    public void setMembersdn(Set<Name> membersdn) {
        this.membersdn = membersdn;
    }

    public List<Usuario> getMembers() {
        return members;
    }

    public void setMembers(List<Usuario> members) {
        this.members = members;
    }

    @JsonIgnore
    public Set<Name> getOwnersdn() {
        return ownersdn;
    }

    public void setOwnersdn(Set<Name> ownersdn) {
        this.ownersdn = ownersdn;
    }

    public List<Usuario> getOwners() {
        return owners;
    }

    public void setOwners(List<Usuario> owners) {
        this.owners = owners;
    }

    @Override
    @JsonIgnore
    public Name getId() {
        return dn;
    }

    @Override
    public boolean isNew() {
        return newFlag;
    }

    public void setNew(boolean flag) {
        this.newFlag = flag;
    }
}
