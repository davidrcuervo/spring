package com.laetienda.usuario.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Prueba {

    @NotNull
    @Size(min=4)
    private String test;

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
