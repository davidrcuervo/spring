package com.laetienda.frontend.controller;

import com.laetienda.frontend.service.FrontendUserService;
import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.service.ThankyouPageService;
import com.laetienda.model.kc.KcUser;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.client.RestClient;

@Controller
@RequestMapping("${seo.user.folder}") //user
public class UserController {
    final private static Logger log = LoggerFactory.getLogger(UserController.class);
    private final RestClient client;

    @Autowired private FormRepository formRepository;
    @Autowired private ThankyouPageService thankYouService;
    @Autowired private RestClientService restClientService;
    @Autowired private Environment env;
    @Autowired private FrontendUserService service;

//    @Value ("${api.usuario.port}") private String usuarioPort;
//    @Value("${api.user.create}") private String apiAddUrl;
//    @Value ("${spring.security.oauth2.client.registration.keycloak.client-id}") private String clientId;

    public UserController(RestClient client){
        this.client = client;
    }

    @GetMapping({"signup.html", "signup"})
    public String signUpGet(Model model){
        return signUp(model, new Usuario());
//        model.addAttribute("form", formRepository.getForm(new Usuario()));
//        return "user/signup";
    }

//    @PostMapping({"signup.html", "signup"})
//    public String signUpPost(@ModelAttribute Usuario user, Model model, HttpServletRequest request){
//        log.trace("(request) username: {}", request.getParameter("username"));
//        log.trace("(ModelAtribute) username: {}", user.getUsername());
//        String result = new String();
//
//        try {
//            Usuario response = restClientService.post(apiAddUrl, user, Usuario.class);
//            log.trace("username: {}", response.getUsername());
//            log.trace("email: {}", response.getEmail());
//            ThankyouPage thankyou = thankYouService.set(new ThankyouPage("/thankyou/user/signup.html", "", "You have succesfully Signed Up!", "Thank you for your interest in our web site.", "/user/login.html", "Log In"));
//            result = "redirect:" + thankyou.getKey();
//        }catch(CustomRestClientExceptionDeprecated e){
//            model.addAttribute("errors", e.getMistake().getErrors());
//            result = signUp(model, user);
//        }
//
//        return result;
//    }

    private String signUp(Model model, Usuario user){
        model.addAttribute("form", formRepository.getForm(user));
        return "user/signup";
    }

    @GetMapping("${seo.user.file}") //user/user.html
    private String account(Model model){
        log.debug("USER_CONTROLLER::account.");
        KcUser user = service.getCurrentUser();
        model.addAttribute("user", user);
        return "user/account";
    }

    @GetMapping("${seo.user.find.file}") //user/find.html/{username}
    private String findUser(@PathVariable String username, Model model){
        log.trace("USER_CONTROLLER::findUser $username: {}", username);
        String address = env.getProperty("api.usuario.login.uri");
        log.trace("USER_CONTROLLER::findUser $address: {}", address);
        String port = env.getProperty("api.usuario.port");
//        Usuario user = client.get().uri(address, port, username).retrieve().toEntity(Usuario.class).getBody();
        String result = client.get().uri(address, port).retrieve().toEntity(String.class).getBody();
        model.addAttribute("result", result);
        return "user/test";
    }

    //TODO: FOR TESTING PORPUSE. MUST BE REMOVED
//    @GetMapping("${api.frontend.user.test}") //user/mock.html/{username}
//    private String testUser(@PathVariable String username, HttpServletRequest request, Model model, Principal principal){
//        log.trace("USER_CONTROLLER::test $username: {}", username);
//
//        //Get JWT Token
//        String authHeader = request.getHeader("Authorization");
//        log.trace("USER_CONTROLLER::test $authToken: {}", authHeader);
//
//        if(principal instanceof OAuth2AuthenticationToken authentication){
//            log.trace("USER_CONTROLLER::test $principal.name: {}", principal.getName());
//            log.trace("USER_CONTROLLER::test $principal.email: {}", authentication.getPrincipal().getAttribute("email").toString());
//            log.trace("USER_CONTROLLER::test $principal.family_nome: {}", authentication.getPrincipal().getAttribute("family_name").toString());
//            log.trace("USER_CONTROLLER::test $principal.given_name: {}", authentication.getPrincipal().getAttribute("given_name").toString());
//
////            Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
////            for(Map.Entry<String, Object> attribute : attributes.entrySet()){
////                log.trace("USER_CONTROLLER::test $attribute: {}", attribute.getKey());
////            }
//
//        }else if(principal != null){
//            log.trace("USER_CONTROLLER::test (non-OAuth2): {}", principal.getName());
//
//        }else{
//            log.trace("USER_CONTROLLER::test No authenticated user found");
//        }
//
//        //Send request to User Resource server
////        String address = env.getProperty("api.usuario.test.uri"); //http://usuarioet:{port}/api/v0/user/mock.html
//        String address = env.getProperty("api.kcUser.login.uri"); //http://usuarioet:{port}login.html/login.html
//        log.trace("USER_CONTROLLER::test $address: {}", address);
//        String result = client.get()
//                .uri(address, usuarioPort, username)
////                .attributes(clientRegistrationId("keycloak"))
//                .retrieve()
//                .toEntity(String.class).getBody();
//        model.addAttribute("result", result);
//        return "user/test";
//    }
}
