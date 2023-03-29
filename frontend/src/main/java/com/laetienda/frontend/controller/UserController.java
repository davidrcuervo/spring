package com.laetienda.frontend.controller;

import com.laetienda.lib.exception.CustomRestClientException;
import com.laetienda.frontend.model.ThankyouPage;
import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.service.ThankyouPageService;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Controller("user")
@RequestMapping("user")
public class UserController {
    final private static Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private FormRepository formRepository;
    @Autowired
    private ThankyouPageService thankyouService;

    @Value("${api.user.create}")
    private String apiAddUrl;
    @Autowired
    private RestClientService service;

    @GetMapping({"signup.html", "signup"})
    public String signUpGet(Model model){
        return signUp(model, new Usuario());
//        model.addAttribute("form", formRepository.getForm(new Usuario()));
//        return "user/signup";
    }

    @PostMapping({"signup.html", "signup"})
    public String signUpPost(@ModelAttribute Usuario user, Model model, HttpServletRequest request){
        log.trace("(request) username: {}", request.getParameter("username"));
        log.trace("(ModelAtribute) username: {}", user.getUsername());
        String result = new String();

        try {
            Usuario response = service.post(apiAddUrl, user, Usuario.class);
            log.trace("username: {}", response.getUsername());
            log.trace("email: {}", response.getEmail());
            ThankyouPage thankyou = thankyouService.set(new ThankyouPage("/thankyou/user/signup.html", "", "You have succesfully Signed Up!", "Thank you for your interest in our web site.", "/user/login.html", "Log In"));
            result = "redirect:" + thankyou.getKey();
        }catch(CustomRestClientException e){
            model.addAttribute("errors", e.getMistake().getErrors());
            result = signUp(model, user);
        }

        return result;
    }

    private String signUp(Model model, Usuario user){
        model.addAttribute("form", formRepository.getForm(user));
        return "user/signup";
    }
}
