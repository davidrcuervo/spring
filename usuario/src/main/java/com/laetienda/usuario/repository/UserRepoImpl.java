package com.laetienda.usuario.repository;

import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class UserRepoImpl implements UserRepository{
    final private static Logger log = LoggerFactory.getLogger(UserRepoImpl.class);

    @Override
    public Usuario find(String username) {
        Usuario result = new Usuario();
        result.setUsername(username);
        return result;
    }

    @Override
    public Usuario findByEmail(String email) {
        Usuario result = new Usuario();
        result.setEmail(email);
        return result;
    }

    @Override
    public List<Usuario> findAll() {
        return new ArrayList<Usuario>();
    }

    @Override
    public Usuario create(Usuario user) {
        return user;
    }

    @Override
    public Usuario edit(Usuario user) {
        return user;
    }

    @Override
    public void delete(Usuario user) {

    }
}
