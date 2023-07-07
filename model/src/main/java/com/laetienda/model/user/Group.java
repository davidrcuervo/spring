package com.laetienda.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laetienda.lib.annotation.HtmlForm;
import com.laetienda.lib.annotation.HtmlInput;
import com.laetienda.lib.interfaces.Forma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Persistable;
import org.springframework.ldap.odm.annotations.*;

import javax.naming.Name;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.*;

@HtmlForm(name = "group")
@Entry(objectClasses = {"groupOfUniqueNames"})
final public class Group implements Persistable, Forma {
    final private static Logger log = LoggerFactory.getLogger(Group.class);
    @Id
    @JsonIgnore
    private Name dn;

    @NotNull @NotEmpty @Size(min = 4, max = 64 )
    @HtmlInput(label = "Name", placeholder = "Entry the name of the group", style_size="col-md-4")
    @Attribute(name = "cn")
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

    @Size(min = 4, max = 255)
    @HtmlInput(label = "Descriptions", placeholder = "Enter small description of the gruup, up to 255 characters", style_size="col-md-8")
    @Attribute(name = "description")
    private String description;
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

        if(!ownersdn.contains(owner.getId())){
            log.trace("3. $owner dn: {}", owner.getId());
            ownersdn.add(owner.getId().toString());
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

        if(!membersdn.contains(member.getId())){
            membersdn.add(member.getId().toString());
        }

        return this;
    }

    public Group removeMember(Usuario user) throws IOException {

        if(ownersdn.contains(user.getId().toString()) || owners.containsKey(user.getUsername())) {
            log.info("User, ({}), is owner and can not be removed", user.getUsername());
            throw new IOException("User can not be removed because he/she is owner");

        }else{
            membersdn.remove(user.getId().toString());
            members.remove(user.getUsername());
        }

        return this;
    }

    public Group removeOwner(Usuario user) throws IOException {

        if(owners.size() <= 1 || ownersdn.size() <= 1){
            String message = String.format("Owner, (%s), is the only owner of group, (%s), and can not be removed", user.getUsername(), name);
            log.warn(message);
            throw new IOException(message);
        }else{
            owners.remove(user.getUsername());
            ownersdn.remove(user.getId().toString());
        }

        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
