package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CrudAction;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

import static com.laetienda.lib.options.CrudAction.*;

public class UserServiceImpl implements UserService {
    final private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    final private String USERNAME = "admuser";
    @Autowired
    private UserRepository repository;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private GroupService gservice;

//    public UserServiceImpl(UserRepository repository){
//        this.repository = repository;
//    }
//
//    public UserServiceImpl(){
////        repository = new UserRepoImpl();
//    }

    @Override
    public UsuarioList findAll() throws NotValidCustomException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth.getAuthorities().contains(new SimpleGrantedAuthority("manager"))){
            return repository.findAll();
        }else{
            throw new NotValidCustomException(
                "User is not authorized",
                    HttpStatus.UNAUTHORIZED,
                    "username"
            );
        }
    }

    @Override
    public Usuario find(String username) throws NotValidCustomException {

        Usuario result = repository.find(username);

        if(result == null){
            throw new NotValidCustomException(
                    String.format("User, (%s), does not exist.", username),
                    HttpStatus.NOT_FOUND,
                    "username"
            );
        }

        return repository.find(username);
    }

    @Override
    public List<Usuario> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    @Override
    public Usuario create(Usuario user) throws NotValidCustomException{
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);

        if(repository.find(user.getUsername()) != null){
            ex.addError("username", "This username is already being used");
            throw ex;
        }

        if(repository.findByEmail(user.getEmail()).size() > 0){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        if(user.getPassword() == null || user.getPassword().isBlank() == true){
            ex.addError("password", "password must not be empty");
            throw ex;
        }

        if(!user.getPassword().equals(user.getPassword2())){
            ex.addError("password2", "password and confirmation password are not equal");
            throw ex;
        }

        return this.save(user, CREATE);
    }

    @Override
    public Usuario update(Usuario user) throws NotValidCustomException {
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);
        Usuario tmpuser = repository.find(user.getUsername());

        if(!user.getEmail().equals(tmpuser.getEmail()) && repository.findByEmail(user.getEmail()).size() > 0){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        return this.save(user, UPDATE);
    }

    private Usuario save(Usuario user, CrudAction action) throws NotValidCustomException{

        Usuario result = user;
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);

        switch(action){

            case CREATE:
                result = repository.create(user);
                break;

            case UPDATE:
                result = repository.update(user);
                break;

            default:
        }

        return result;
    }

    @Override
    public void delete(Usuario user) throws NotValidCustomException {
        //TODO test is not last owner of a group

        //TODO Test if user is manager

        //Test if it is tomcat or admuser
        if(user.getUsername().equalsIgnoreCase("admuser") || user.getUsername().equalsIgnoreCase("tomcat")){
            String message = String.format("User, (%s), can't be removed from the system", user.getUsername());
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "user");
        }

        repository.delete(user);
    }

    @Override
    public GroupList authenticate(Usuario user) throws NotValidCustomException {
        GroupList result = null;
        this.find(user.getUsername());

        if(repository.authenticate(user)){
            result = gservice.findAllByMember(user);
        }

        return result;
    }
}
