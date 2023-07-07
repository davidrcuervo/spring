package com.laetienda.usuario.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository  {

//    Usuario find(String username);
//    List<Usuario> findByEmail(String email);
//    Usuario create(Usuario user);
//    Usuario update(Usuario user);
//    Usuario deleteToken(String username, String token);
//    Usuario findByToken(String token);
//    void delete(Usuario user) throws NotValidCustomException;
    UsuarioList findAll();
    boolean authenticate(Usuario user);
}
