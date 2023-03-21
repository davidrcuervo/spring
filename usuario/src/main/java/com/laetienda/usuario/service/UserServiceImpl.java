package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CrudAction;
import com.laetienda.model.user.Group;
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

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.laetienda.lib.options.CrudAction.*;

public class UserServiceImpl implements UserService {
    final private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    final private String USERNAME = "admuser";
    @Autowired
    private UserRepository repository;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private GroupService gService;

    @Autowired
    private GroupRepository gRepo;

//    public UserServiceImpl(UserRepository repository){
//        this.repository = repository;
//    }
//
//    public UserServiceImpl(){
////        repository = new UserRepoImpl();
//    }

    @Override
    public UsuarioList findAll() throws NotValidCustomException {

        if(isUserInRole("manager")){
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

        if(request.getUserPrincipal().getName().equals(username) || isUserInRole("manager")){
            Usuario result = repository.find(username);
            if(result == null){
                throw new NotValidCustomException(
                        String.format("User, (%s), does not exist.", username),
                        HttpStatus.NOT_FOUND,
                        "username"
                );
            }

        }else{
            throw new NotValidCustomException(
                    HttpStatus.UNAUTHORIZED.toString(),
                    HttpStatus.UNAUTHORIZED,
                    "username"
            );
        }

        return repository.find(username);
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().contains(new SimpleGrantedAuthority(role));
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

        //persist user in LDAP Directory
        Usuario result =  this.save(user, CREATE);

        //TODO add user to valid group
        return result;
    }

    @Override
    public Usuario update(Usuario user) throws NotValidCustomException {
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);
        Usuario tmpuser = repository.find(user.getUsername());

        if(!user.getEmail().equals(tmpuser.getEmail()) && repository.findByEmail(user.getEmail()).size() > 0){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        //Persist user in LDAP directory
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
    public void delete(String username) throws NotValidCustomException {
        //find(username) test if logged user can remove the user
        Usuario user = find(username);

        //Test if it is tomcat or admuser
        if(user.getUsername().equalsIgnoreCase("admuser") || user.getUsername().equalsIgnoreCase("tomcat")){
            String message = String.format("User, (%s), can't be removed from the system", user.getUsername());
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "user");
        }

        //TODO Test if user is manager

        //Test if user is not last owner of a group
        GroupList temp = gRepo.findAllByOwner(username);
        for(Map.Entry<String, Group> entry : temp.getGroups().entrySet()){
            if(entry.getValue().getOwners().size() < 2) {
                throw new NotValidCustomException("Can't remove last user of a group", HttpStatus.FORBIDDEN, "user");
            }
        }

        try{
            //remove user from group where is owner
            for(Map.Entry<String, Group> entry : temp.getGroups().entrySet()) {
                gRepo.removeOwner(entry.getValue(), user);
//                entry.getValue().removeOwner(user);
            }

            //Remove user from groups where is member
            temp = gRepo.findAllByMember(username);
            for(Map.Entry<String, Group> entry : temp.getGroups().entrySet()){
                log.trace("removing user from group: {}", entry.getKey());
                gRepo.removeMember(entry.getValue(), user);
//                entry.getValue().removeMember(user);
            }

            //remove user
            repository.delete(user);
        }catch (IOException e) {
            throw new NotValidCustomException("Failed to remove user", HttpStatus.INTERNAL_SERVER_ERROR, "user", e.getMessage());
        }
    }

    @Override
    public GroupList authenticate(Usuario user) throws NotValidCustomException {
        GroupList result = null;

        if(repository.find(user.getUsername()) == null) {
            throw new NotValidCustomException(
                HttpStatus.NOT_FOUND.toString(),
                HttpStatus.NOT_FOUND,
                "username"
            );
        }else if(repository.authenticate(user)){
            result = gService.findAllByMember(user);
        }

        return result;
    }
}
