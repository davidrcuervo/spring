package com.laetienda.model.schema;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="ITEM")
public class DbItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name="owner", nullable = false)
    private String owner;

    @NotNull
    @Column(name="group", nullable = false)
    private String grupo;

    public DbItem(){}

    public DbItem(String owner, String group) {
        this.owner = owner;
        this.grupo = group;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull String getOwner() {
        return owner;
    }

    public void setOwner(@NotNull String owner) {
        this.owner = owner;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }
}
