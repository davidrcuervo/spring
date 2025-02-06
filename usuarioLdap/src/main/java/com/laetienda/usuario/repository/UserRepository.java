package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.stereotype.Repository;

import javax.naming.Name;

public interface UserRepository  {

    Usuario findbyUsername(String username);
//    List<Usuario> findByEmail(String email);
//    Usuario create(Usuario user);
//    Usuario update(Usuario user);
//    Usuario deleteToken(String username, String token);
//    Usuario findByToken(String token);
//    void delete(Usuario user) throws NotValidCustomException;
    Usuario findById(Name id);
    UsuarioList findAll();
    boolean authenticate(Usuario user);
    Usuario modifyAtrribute(String username, String attribute, String value, int action);
}
