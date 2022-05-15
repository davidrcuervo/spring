package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    public UsuarioList findAll();
    public Usuario find(String username);
    public List<Usuario> findByEmail(String email);
    public Usuario create(Usuario user) throws NotValidCustomException;
    public Usuario update(Usuario user) throws NotValidCustomException;
    public void delete(Usuario user) throws NotValidCustomException;
}
