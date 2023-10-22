package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public interface UserService {

    public UsuarioList findAll() throws NotValidCustomException;
    public Usuario find(String username) throws NotValidCustomException;
    public List<Usuario> findByEmail(String email);
    public Usuario create(Usuario user) throws NotValidCustomException;
    public Usuario update(Usuario user) throws NotValidCustomException;
    public void delete(String username) throws NotValidCustomException;
    public Usuario emailValidation(String encToken) throws NotValidCustomException;
    GroupList authenticate(Usuario user) throws NotValidCustomException;
    String requestPasswordRecovery(String username) throws NotValidCustomException;
    Usuario passwordRecovery(String encToken, Map<String, String> params) throws NotValidCustomException;
}
