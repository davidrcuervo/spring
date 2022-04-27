package com.laetienda.frontend.controller;

import com.laetienda.frontend.model.ThankyouPage;
import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.frontend.service.ThankyouPageService;
import com.laetienda.frontend.service.UserService;
import com.laetienda.model.user.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller("user")
@RequestMapping("user")
public class UserController {
    final private static Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private FormRepository formRepository;
    @Autowired
    private ThankyouPageService thankyouService;

    @Autowired
    private UserService service;

    @GetMapping({"signup.html", "signup"})
    public String signUpGet(Model model){
        model.addAttribute("form", formRepository.getForm(new Usuario()));
        return "user/signup";
    }

    @PostMapping({"signup.html", "signup"})
    public String signUpPost(@ModelAttribute Usuario user, HttpServletRequest request){
        log.debug("(request) username: {}", request.getParameter("username"));
        log.debug("(ModelAtribute) username: {}", user.getUsername());
        Usuario result = service.post(user);
        ThankyouPage thankyou = thankyouService.set(new ThankyouPage("/thankyou/user/signup.html", "", "You have succesfully Signed Up!", "Thank you for your interest in our web site.", "/user/login.html", "Log In"));
        return "redirect:" + thankyou.getKey();
    }
}
