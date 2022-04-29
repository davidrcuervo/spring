package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.user.Usuario;
import com.laetienda.usuario.repository.UserRepoImpl;
import com.laetienda.usuario.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class UserServiceImpl implements UserService {
    final private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository repository;

    public UserServiceImpl(UserRepository repository){
        this.repository = repository;
    }

    public UserServiceImpl(){
        repository = new UserRepoImpl();
    }

    @Override
    public Usuario find(String username) {
        return repository.find(username);
    }

    @Override
    public Usuario create(Usuario user) throws NotValidCustomException{
        Usuario result = user;
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);

        if(!user.getPassword().equals(user.getPassword2())){
            ex.addError("password2", "password and confirmation password are not equal");
            throw ex;
        }

        if(repository.find(user.getUsername()) != null){
            ex.addError("username", "This username is already being used");
            throw ex;
        }

        if(repository.findByEmail(user.getEmail()) != null){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        return repository.create(user);
    }

    @Override
    public Usuario edit(Usuario user) {
        return repository.edit(user);
    }

    @Override
    public void delete(Usuario user) {
        repository.delete(user);
    }
}
