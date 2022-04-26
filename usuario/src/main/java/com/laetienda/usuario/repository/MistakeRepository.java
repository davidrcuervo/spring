package com.laetienda.usuario.repository;

import com.laetienda.lib.model.Mistake;

public interface MistakeRepository {

    public void addMistake(String name, String message);
    public Mistake get();
}
