package com.laetienda.model.company;

import com.laetienda.model.schema.DbItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import net.minidev.json.annotate.JsonIgnore;
import org.checkerframework.common.aliasing.qual.Unique;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Company extends DbItem {

    @NotNull @Unique
    @Size(min = 2, max = 64)
    private String name;

    @Size(min = 3, max = 400)
    private String description;

    @Email
    private String email;

    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$")
    private String phone;

    @Size(min=5, max=64)
    private String address;

//    @JsonIgnore
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Member> members = new ArrayList<Member>();

    public Company(){}

    public Company(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Company setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Company setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Company setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public Company setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Company setAddress(String address) {
        this.address = address;
        return this;
    }

    @JsonIgnore
    public List<Member> getMembers() {
        return members;
    }

    public Company setMembers(@NotNull List<Member> members) {
        this.members = members;
        return this;
    }

    public Company addMember(@NotNull Member member){
        if(member == null){
            this.members = new ArrayList<Member>();
        }

        if(members.stream().noneMatch(m -> m.getUserId().equals(member.getUserId()))){
            this.members.add(member);
        }

        return this;
    }

    public Company removeMember(Member member){
        members.removeIf(m -> m.getUserId().equals(member.getUserId()));
        return this;
    }
}
