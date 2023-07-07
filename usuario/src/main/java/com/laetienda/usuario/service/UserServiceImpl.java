package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CrudAction;
import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.SpringUserRepository;
import com.laetienda.usuario.repository.UserRepository;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.lib.service.ToolBoxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

import javax.naming.Name;
import java.io.IOException;
import java.util.*;

import static com.laetienda.lib.options.CrudAction.*;

public class UserServiceImpl implements UserService {
    final private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    final private String USERNAME = "admuser";

    @Autowired
    private UserRepository repository;

    @Autowired
    private SpringUserRepository springRepository;

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private GroupService gService;

    @Autowired
    private GroupRepository gRepo;

    @Autowired
    private LdapDn dn;

    @Autowired
    private RestClientService client;

    @Autowired
    private ToolBoxService tb;

    @Value("${api.mailer.send}")
    private String urlSendMessage;

    @Value("${api.frontend.user.emailValidation}")
    private String urlFrontendEmailValidation;

    @Value("${api.frontend.user.passwordrecovery}")
    private String urlFrontendUserPasswordRecovery;

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
        Usuario result = null;
        if(request.getUserPrincipal().getName().equals(username) || isUserInRole("ROLE_MANAGER")){
//            Usuario result = repository.find(username);
            result = springRepository.findByUsername(username);
            log.trace("User found. Full Name: {}", result.getFullName());
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

        return result;
    }

    private boolean isUserInRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().contains(new SimpleGrantedAuthority(role));
    }

    @Override
    public List<Usuario> findByEmail(String email) {
        return springRepository.findByEmail(email);
    }

    @Override
    public Usuario create(Usuario user) throws NotValidCustomException{
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);

        if(springRepository.findByUsername(user.getUsername()) != null){
            ex.addError("username", "This username is already being used");
            throw ex;
        }

        List<Usuario> uEmails = springRepository.findByEmail(user.getEmail());
        log.trace("uMails.size = {}", uEmails.size());

        if(uEmails != null && uEmails.size() > 0){
//        if(repository.findByEmail(user.getEmail()).size() > 0){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        if(!isValidPassword(user.getPassword(), user.getPassword2())){
            ex.addError("password", "password must not be empty or they are not equal");
            throw ex;
        }

        if(user.getToken() != null){
            ex.addError("usuario", "Weird error user already has a token");
            throw ex;
        }

        String token = setTokenAndSendEmail(user, "default/emailConfirmation.html", "Welcome, please confirm your contact information", urlFrontendEmailValidation);

        if(token != null){
            user.setToken(token);
        }

        user.setNew(true);
        user.setId(dn.getUserDn(user.getUsername()));

        return springRepository.save(user);
    }

    @Override
    public Usuario update(Usuario user) throws NotValidCustomException {
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);
        Usuario tmpuser = springRepository.findByUsername(user.getUsername());

        if(!user.getEmail().equals(tmpuser.getEmail()) && springRepository.findByEmail(user.getEmail()).size() > 0){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        //Persist user in LDAP directory
        return springRepository.save(user);
    }

//    private Usuario save(Usuario user, CrudAction action) throws NotValidCustomException{
//
//        Usuario result = user;
//        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);
//
//        switch(action){
//
//            case CREATE:
//                result = repository.create(user);
//                break;
//
//            case UPDATE:
//                result = repository.update(user);
//                break;
//
//            default:
//        }
//
//        return result;
//    }

    @Override
    public void delete(String username) throws NotValidCustomException {
        //find(username) test if logged user can remove the user
        Usuario user = find(username);

        if(user == null){
            String message = String.format("User, (%s), does not exist and can't be removed", username);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "user");
        }

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
            springRepository.delete(user);
        }catch (IOException e) {
            throw new NotValidCustomException("Failed to remove user", HttpStatus.INTERNAL_SERVER_ERROR, "user", e.getMessage());
        }
    }

    @Override
    public Usuario emailValidation(String encToken) throws NotValidCustomException {
        String token = tb.decrypt(encToken, System.getProperty("jasypt.encryptor.password"));
        log.trace("validating user email. $token: {}", token);
        Usuario user = springRepository.findByToken(token);
        String username = request.getUserPrincipal().getName();

        if(user == null){
            throw new NotValidCustomException("Not valid token to confirm email address.", HttpStatus.BAD_REQUEST, "user");

        }else if(!username.toLowerCase().equals(user.getUsername().toLowerCase())){
            throw new NotValidCustomException("Link token does not below to authenticated user", HttpStatus.CONFLICT, "user");

        }else {
            gService.addMemberToValidUserAccounts(username);
            user.setToken(null);
            return springRepository.save(user);
        }

    }

    @Override
    public GroupList authenticate(Usuario user) throws NotValidCustomException {
        GroupList result = null;

        if(springRepository.findByUsername(user.getUsername()) == null) {
            log.trace("invalid user. {}", user.getUsername());
            throw new NotValidCustomException(
                HttpStatus.NOT_FOUND.toString(),
                HttpStatus.NOT_FOUND,
                "username"
            );
        }else if(repository.authenticate(user)){
            result = gService.findAllByMember(user);
        }else{
            log.trace("Invalid credentials. User: {}", user.getUsername());
        }

        return result;
    }

    @Override
    public String requestPasswordRecovery(String username) throws NotValidCustomException {

        Usuario user = springRepository.findByUsername(username);

        String token = setTokenAndSendEmail(user, "default/passwordrecovery.html", "Welcome Back! Please reset your password", urlFrontendUserPasswordRecovery);
        user.setToken(token);

        springRepository.save(user);
        return token;
    }

    @Override
    public Usuario passwordRecovery(String encToken, Map<String, String> params) throws NotValidCustomException {
        String token = tb.decrypt(encToken, System.getProperty("jasypt.encryptor.password"));
        log.trace("Decrypted token: $token: {}", token);

        Usuario result = springRepository.findByToken(token);
        if(result == null)
            throw new NotValidCustomException("Not valid token", HttpStatus.BAD_REQUEST, "User");

        if(isValidPassword(params.get("password"), params.get("password2"))) {
             result.setPassword(params.get("password"));
             result.setToken(null);
             result = springRepository.save(result);

        }else{
            throw new NotValidCustomException("Not valid password", HttpStatus.BAD_REQUEST, "password");
        }

        return result;
    }

    private Boolean isValidPassword(String password, String password2) throws NotValidCustomException{

        Boolean result = false;

        if(password == null || password.isBlank() == true){
            throw new NotValidCustomException( "password must not be empty", HttpStatus.BAD_REQUEST,"password");
        } else if(!password.equals(password2)){
            throw new NotValidCustomException("password and confirmation password are not equal", HttpStatus.BAD_REQUEST, "password2");
        } else {
            result = true;
        }

        return result;
    }

    private String setTokenAndSendEmail(Usuario user, String view, String subject, String urlValue) throws NotValidCustomException{
        String result = null;

        //CREATE USER TOKEN
        Usuario usrToken = null;
        String token;

        do{
            token = tb.newToken(12);
            usrToken = springRepository.findByToken(token);
        }while(usrToken != null);

        user.setToken(token);
        String encToken = tb.encrypt(token, System.getProperty("jasypt.encryptor.password"));

        //SEND MAILER TO CONFIRM USER PARAMETERS
        EmailMessage message = new EmailMessage();
        Map<String, Object> variables = new HashMap<>();
        String urlEvaluated = urlValue.replaceFirst("\\{encToken\\}",encToken);
        log.trace("email validation link: {}", urlEvaluated);
        variables.put("link", urlEvaluated);

        message.setVariables(variables);
        message.setView(view);
        message.setSubject(subject);
        message.setTo(new HashSet<String>(Arrays.asList(user.getEmail())));
        String resp = client.send(urlSendMessage, HttpMethod.POST, message, String.class, null);

        if(Boolean.valueOf(resp)){
            log.trace("Token and email has been sent successfully");
            result = token;
        }else{
            throw new NotValidCustomException("Failed to send email.", HttpStatus.INTERNAL_SERVER_ERROR, "user");
        }

        return result;
    }
}
