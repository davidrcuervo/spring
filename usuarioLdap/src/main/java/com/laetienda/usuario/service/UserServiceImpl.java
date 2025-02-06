package com.laetienda.usuario.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.messager.EmailMessage;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import com.laetienda.usuario.lib.LdapDn;
import com.laetienda.usuario.repository.GroupRepository;
import com.laetienda.usuario.repository.SpringGroupRepository;
import com.laetienda.usuario.repository.SpringUserRepository;
import com.laetienda.usuario.repository.UserRepository;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.utils.service.api.MessengerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

import javax.naming.InvalidNameException;
import javax.naming.directory.DirContext;
import java.io.IOException;
import java.util.*;

public class UserServiceImpl implements UserService {
    final private static Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    final private String USERNAME = "admuser";

    @Autowired
    private UserRepository repository;

    @Autowired private SpringUserRepository springRepository;
    @Autowired private SpringGroupRepository springGroupRepository;
    @Autowired private HttpServletRequest request;
    @Autowired private GroupService gService;
    @Autowired private GroupRepository gRepo;
    @Autowired private LdapDn dn;
    @Autowired private Environment env;
    @Autowired private RestClientService client;
    @Autowired private ToolBoxService tb;
    @Autowired private MessengerApi messengerApi;

    @Value("${api.frontend.user.emailValidation}")
    private String urlFrontendEmailValidation;

    @Value("${api.frontend.user.passwordrecovery}")
    private String urlFrontendUserPasswordRecovery;

    @Value("${admuser.username}")
    private String admuser;

    @Value("${backend.username}")
    private String backendUsername;

    @Override
    public UsuarioList findAll() throws NotValidCustomException {
        String username = request.getUserPrincipal().getName();
        if(isUserInRole("ROLE_MANAGER") || backendUsername.equals(username)){
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
        log.debug("USER_SERVICE::find. $username: {}", username);
        Usuario result = springRepository.findByUsername(username);

        if(result == null){
            throw new NotValidCustomException(
                    String.format("User, (%s), does not exist.", username),
                    HttpStatus.NOT_FOUND,
                    "username"
            );
        }

        if(canRead(result)){
            log.trace("USER_SERVICE::find. $userDn: {}", dn.getFullDn(result.getId()));
            return result;
        }else{
            throw new NotValidCustomException(
                    HttpStatus.UNAUTHORIZED.toString(),
                    HttpStatus.UNAUTHORIZED,
                    "username");
        }
//        if(request.getUserPrincipal().getName().equals(username) || isUserInRole("ROLE_MANAGER")){
//            Usuario result = repository.find(username);
//            result = springRepository.findByUsername(username);
//            result = repository.findbyUsername(username);

//            if(result == null){
//                throw new NotValidCustomException(
//                        String.format("User, (%s), does not exist.", username),
//                        HttpStatus.NOT_FOUND,
//                        "username"
//                );
//            }else{
//                log.trace("User found. Full Name: {}", result.getFullName());
//            }

//        }else{
//            throw new NotValidCustomException(
//                    HttpStatus.UNAUTHORIZED.toString(),
//                    HttpStatus.UNAUTHORIZED,
//                    "username"
//            );
//        }
    }

    private boolean isUserInRole(String role_name) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        log.trace("$role_name: {}", role_name);
//        String role = "ROLE_" + role_name.toUpperCase();
        log.trace("Required role: {}", role_name);

//        for(GrantedAuthority t : auth.getAuthorities()){
//            log.trace("User has role. $role: {}", t.getAuthority());
//        }

        return auth.getAuthorities().contains(new SimpleGrantedAuthority(role_name));
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

        if(request.getUserPrincipal() != null){
            String message = String.format("Not possible to create users while another user is logged in. $loginusername: %s", request.getUserPrincipal().getName());
            ex.addError("usuario", message);
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

        String token = getToken();
        user.setEncToken(tb.encrypt(token, System.getProperty("jasypt.encryptor.password")));

        //SEND EMAIL, first check if profile is production
        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if(profiles.contains("production")){
            sendEmail(user, "default/emailConfirmation.html", "Welcome, please confirm your contact information", urlFrontendEmailValidation);
        }

        if(token != null){
            user.setToken(token);
        }

        user.setNew(true);
//        user.setDn(dn.getUserDn(user.getUsername()));
        return springRepository.save(user);
    }

    @Override
    public Usuario update(Usuario user) throws NotValidCustomException {
//        user.setDn(dn.getUserDn(user.getUsername()));
        Usuario tmpuser = springRepository.findByUsername(user.getUsername());

        if(!(request.getUserPrincipal().getName().equals(user.getUsername()) || isUserInRole("ROLE_MANAGER"))){
            throw new NotValidCustomException(
                    HttpStatus.UNAUTHORIZED.toString(),
                    HttpStatus.UNAUTHORIZED,
                    "username"
            );
        }

        if(!user.getEmail().equals(tmpuser.getEmail())){
            user = updateEmail(user);
        }

        //Persist user in LDAP directory
        user.setDn(tmpuser.getId());
        user.setNew(false);
        return springRepository.save(user);
    }

    private Usuario updateEmail(Usuario user) throws NotValidCustomException {
        NotValidCustomException ex = new NotValidCustomException("Failed to persist user", HttpStatus.BAD_REQUEST);

        if(springRepository.findByEmail(user.getEmail()).size() > 0){
            ex.addError("email", "This email address has been already registered");
            throw ex;
        }

        //CREATE TOKEN AND SEND EMAIL TO VALIDATE ADDRESS
        String token = getToken();
        user.setEncToken(tb.encrypt(token, System.getProperty("jasypt.encryptor.password")));
        sendEmail(user, "default/emailConfirmation.html", "Welcome, please confirm your contact information", urlFrontendEmailValidation);
        user.setToken(token);

        //DISABLE ACCOUNT
        try {
            Group group = gRepo.findByName("validUserAccounts");
            group.removeMember(user);
            springGroupRepository.save(group);
        } catch (IOException e) {
            ex.addError("user", e.getMessage());
        }

        return user;
    }

    @Override
    public void delete(String username) throws NotValidCustomException {
        log.debug("USER_SERVICE::Delete. $username: {}", username);

        //find(username) test if logged user can remove the user
        Usuario user = find(username);

        if(user == null){
            String message = String.format("User, (%s), does not exist and can't be removed", username);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "user");
        }

        //Test if it is trying to remove admuser
        if(username.toLowerCase().equals(admuser.toLowerCase())){
            String message = String.format("Administrator user, %s, can't be removed.", username);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "user");
        }

        //Test if it is self user or manager
        if(!(request.getUserPrincipal().getName().equals(username) || isUserInRole("ROLE_MANAGER"))){
            String message = String.format("User, (%s), can't be removed from the system", user.getUsername());
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "user");
        }

        //Test if user is not last owner of a group
        GroupList temp = gRepo.findAllByOwner(username);
        for(Map.Entry<String, Group> group : temp.getGroups().entrySet()){
            log.trace("User, {}, is owner of group, {}.", username, group.getKey());
            if(group.getValue().getOwners().size() < 2) {
                throw new NotValidCustomException("Can't remove last owner of a group", HttpStatus.FORBIDDEN, "user");
            }
        }

        //remove user from group where is owner
        for(Map.Entry<String, Group> entry : temp.getGroups().entrySet()) {
            gService.removeOwner(entry.getKey(), username);
//                gRepo.removeOwner(entry.getValue(), user);
//                entry.getValue().removeOwner(user);
        }

        //Remove user from groups where is member
        temp = gRepo.findAllByMember(username);
        for(Map.Entry<String, Group> entry : temp.getGroups().entrySet()){
            log.trace("USER_SERVICE::removing user from group: {}", entry.getKey());
            gService.removeMember(entry.getKey(), username);
//                gRepo.removeMember(entry.getValue(), user);
//                entry.getValue().removeMember(user);
        }

        //remove user
        springRepository.delete(user);
    }

    @Override
    public Usuario emailValidation(String encToken) throws NotValidCustomException {
        String token = tb.decrypt(encToken, System.getProperty("jasypt.encryptor.password"));
        log.trace("USER_SERVICE::emailValidation. $token: {}", token);
        Usuario user = springRepository.findByToken(token);
        String username = request.getUserPrincipal().getName();

        if(user == null){
            throw new NotValidCustomException("Not valid token to confirm email address.", HttpStatus.BAD_REQUEST, "user");

        }else if(!username.toLowerCase().equals(user.getUsername().toLowerCase())){
            throw new NotValidCustomException("Link token does not below to authenticated user", HttpStatus.CONFLICT, "user");

        }else {
            Group group = springGroupRepository.findByName("validUserAccounts");
            user.setDn(dn.getFullDn(user.getId()));
            group.addMember(user);
            log.trace("USER_SERVICE::emailValidation. $userDn: {}", springRepository.findByUsername(user.getUsername()));
            springGroupRepository.save(group);

            user = springRepository.findByUsername(user.getUsername());
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
            String message = String.format("Invalid credentials. User: %s", user.getUsername());
            log.info(message);
            throw new NotValidCustomException(message, HttpStatus.UNAUTHORIZED, "password");
        }

        return result;
    }

    @Override
    public String requestPasswordRecovery(String username) throws NotValidCustomException {

        Usuario user = springRepository.findByUsername(username);

        String token = getToken();
        user.setEncToken(tb.encrypt(token, System.getProperty("jasypt.encryptor.password")));

        List<String> profiles = Arrays.asList(env.getActiveProfiles());
        if(profiles.contains("production")) {
            sendEmail(user, "default/passwordrecovery.html", "Welcome Back! Please reset your password", urlFrontendUserPasswordRecovery);
        }

        user.setToken(token);

        springRepository.save(user);
        return token;
    }

    @Override
    public Usuario passwordRecovery(String encToken, Map<String, String> params) throws NotValidCustomException {
        String token = tb.decrypt(encToken, System.getProperty("jasypt.encryptor.password"));
        log.trace("Decrypted token: $token: {}", token);
        String newPass = params.get("password");

        Usuario tmpUser = springRepository.findByToken(token);

        if(tmpUser == null)
            throw new NotValidCustomException("Not valid token", HttpStatus.BAD_REQUEST, "User");

        if(isValidPassword(newPass, params.get("password2"))) {
            String username = tmpUser.getUsername();
//             result.setPassword(params.get("password"));
             repository.modifyAtrribute(username, "userPassword", newPass, DirContext.REPLACE_ATTRIBUTE);
             repository.modifyAtrribute(username, "labeledURI", null, DirContext.REMOVE_ATTRIBUTE);
//             result.setToken(null);
//             result = springRepository.save(result);
            return springRepository.findByUsername(username);

        }else{
            throw new NotValidCustomException("Not valid password", HttpStatus.BAD_REQUEST, "password");
        }
    }

    @Override
    public String getApplicationProfile() {
        String result = new String();

        for(String profile : env.getActiveProfiles())
            result += profile;

        return result;
    }

    private Boolean canRead(Usuario user){
        String loginUsername = request.getUserPrincipal().getName();

        if(loginUsername.equals(user.getUsername())
                || isUserInRole("ROLE_MANAGER")
                || admuser.equals(loginUsername)
                || backendUsername.equals(loginUsername)
        ){
            return true;
        }else{
            return false;
        }
    }

    private Boolean canEdit(){
        return false;
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

    private String getToken(){

        String result = null;
        Usuario usrToken = null;

        do{
            result = tb.newToken(12);
            log.trace("user token. $token: {}", result);
            usrToken = springRepository.findByToken(result);
        }while(usrToken != null);

        return result;
    }

    private void sendEmail(Usuario user, String view, String subject, String urlValue) throws NotValidCustomException{

        String urlEvaluated = urlValue.replaceFirst("\\{encToken\\}", user.getEncToken());
        Map<String, Object> variables = new HashMap<String, Object>() {{put("link", urlEvaluated);}};

        //SEND MAILER TO CONFIRM USER PARAMETERS
        EmailMessage message = new EmailMessage();

        message.setVariables(variables);
        message.setView(view);
        message.setSubject(subject);
        message.setTo(new HashSet<String>(Arrays.asList(user.getEmail())));

        String resp = messengerApi.sendMessage(message).getBody();

        if(Boolean.valueOf(resp)){
            log.trace("Token and email has been sent successfully");
        }else{
            throw new NotValidCustomException("Failed to send email.", HttpStatus.INTERNAL_SERVER_ERROR, "user");
        }
    }
}
