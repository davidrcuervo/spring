package com.laetienda.usuario.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository  {

    public Usuario find(String username);
    public List<Usuario> findByEmail(String email);
    public List<Usuario> findAll();
    public Usuario create(Usuario user);
    public Usuario update(Usuario user);
    public void delete(Usuario user) throws NotValidCustomException;
}
