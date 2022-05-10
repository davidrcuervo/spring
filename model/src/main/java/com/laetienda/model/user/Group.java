package com.laetienda.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;

@Entry(objectClasses = {"groupOfUniqueNames"})
final public class Group implements Persistable {
    final private static Logger log = LoggerFactory.getLogger(Group.class);
    @Id
    @JsonIgnore
    private Name dn;

    @NotNull @NotEmpty @Size(min = 4, max = 64)
    @Attribute(name = "cn")
//    @DnAttribute(value = "cn", index = 1)
    private String name;

    @Attribute(name = "owner")
    @JsonIgnore
    private Set<String> ownersdn;

    @Transient
    private Map<String, Usuario> owners;

    @Attribute(name = "uniqueMember")
    @JsonIgnore
    private Set<String> membersdn;

    @Transient
    private Map<String, Usuario> members;

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
    public Set<String> getMembersdn() {
        return membersdn;
    }

    public void setMembersdn(Set<String> membersdn) {
        this.membersdn = membersdn;
    }

    public Map<String, Usuario> getMembers() {
        return members;
    }

    public void setMembers(Map<String, Usuario> members) {
        this.members = members;
    }

    @JsonIgnore
    public Set<String> getOwnersdn() {
        return ownersdn;
    }

    public void setOwnersdn(Set<String> ownersdn) {
        this.ownersdn = ownersdn;
    }

    public Map<String, Usuario> getOwners() {
        return owners;
    }

    public void setOwners(Map<String, Usuario> owners) {
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

    public Group addOwner(Usuario owner){
        if(owners == null){
            owners = new HashMap<>();
        }

        if(ownersdn == null){
            ownersdn = new HashSet<>();
        }

        if(owners.get(owner.getUsername()) == null){
            owners.put(owner.getUsername(), owner);
        }

        if(!ownersdn.contains(owner.getDn())){
            log.trace("3. $owner dn: {}", owner.getDn());
            ownersdn.add(owner.getDn().toString());
        }

        addMember(owner);
        return this;
    }

    public Group addMember(Usuario member){
        if(members == null){
            members = new HashMap<>();
        }

        if(membersdn == null){
            membersdn = new HashSet<>();
        }

        if(members.get(member.getUsername()) == null){
            members.put(member.getUsername(), member);
        }

        if(!membersdn.contains(member.getDn())){
            membersdn.add(member.getDn().toString());
        }

        return this;
    }
}
