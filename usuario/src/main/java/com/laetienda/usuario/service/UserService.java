package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Service;

@Service
public interface UserService {

    public Usuario find(String username);
    public Usuario create(Usuario user) throws NotValidCustomException;
    public Usuario edit(Usuario user);
    public void delete(Usuario user);
}
