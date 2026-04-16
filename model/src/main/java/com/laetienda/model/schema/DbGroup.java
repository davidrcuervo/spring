package com.laetienda.model.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.lib.options.DbServiceAccessPolicy;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
public class DbGroup {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @Size(min=1, max = 128)
    @Column(unique = true, nullable = false)
    private String name;

    @JsonIgnore
    @ManyToMany(
            mappedBy = "readerGroups"
    )
    private Set<DbItem> readerItems;

    @JsonIgnore
    @ManyToMany(
            mappedBy = "editorGroups"
    )
    private Set<DbItem> editorItems;

    @NotNull
    private String owner;

    @NotNull
    @Enumerated(EnumType.STRING)
    private DbUserAccessPolicy userAccessPolicy;

    @ElementCollection
    @CollectionTable(name="ITEM_GROUP_MEMBER", joinColumns = @JoinColumn(name = "ITEM_GROUP_ID"))
    private Set<String> members;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private DbServiceAccessPolicy serviceAccessPolicy;

    public DbGroup() {

    }

    public DbGroup(String name){
        this.setName(name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DbGroup setName(String name) {
        this.name = name;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public DbUserAccessPolicy getUserAccessPolicy() {
        return userAccessPolicy;
    }

    public DbGroup setUserAccessPolicy(DbUserAccessPolicy userAccessPolicy) {
        this.userAccessPolicy = userAccessPolicy;
        return this;
    }

    public Set<String> getMembers() {
        if(members == null) {
            members = new HashSet<>();
        }
        return members;
    }

    public void setMembers(Set<String> members) {
        this.members = members;
    }

    public void addMember(String member){
        if(members == null){
            members = new HashSet<String>();
        }

        members.add(member);
    }

    public void removeMember(String member){
        if(members != null){
            members.remove(member);
        }
    }

    public Set<DbItem> getReaderItems() {
        return readerItems;
    }

    public void addReaderItem(DbItem item) {
        if(readerItems == null){
            readerItems = new HashSet<>();
        }

        readerItems.add(item);
    }

    public Set<DbItem> getEditorItems() {
        return editorItems;
    }

    public void addEditorItem(DbItem item) {
        if(editorItems == null){
            editorItems = new HashSet<>();
        }

        editorItems.add(item);
    }

    public DbServiceAccessPolicy getServiceAccessPolicy() {
        return serviceAccessPolicy;
    }

    public void setServiceAccessPolicy(DbServiceAccessPolicy serviceAccessPolicy) {
        this.serviceAccessPolicy = serviceAccessPolicy;
    }
}
