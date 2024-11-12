package com.laetienda.model.schema;

import jakarta.persistence.Entity;

@Entity
public class ItemTypeA extends DbItem{

    private Integer age;
    private String address;
    private String username;

    public Integer getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
