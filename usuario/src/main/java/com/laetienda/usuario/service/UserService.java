package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    public UsuarioList findAll() throws NotValidCustomException;
    public Usuario find(String username) throws NotValidCustomException;
    public List<Usuario> findByEmail(String email);
    public Usuario create(Usuario user) throws NotValidCustomException;
    public Usuario update(Usuario user) throws NotValidCustomException;
    public void delete(Usuario user) throws NotValidCustomException;

    GroupList authenticate(Usuario user) throws NotValidCustomException;
}
