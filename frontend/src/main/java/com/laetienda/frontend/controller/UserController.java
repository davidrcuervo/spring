package com.laetienda.frontend.controller;

import com.laetienda.frontend.model.Form;
import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.model.user.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("user")
@RequestMapping("user")
public class UserController {

    @Autowired
    private FormRepository formRepository;

    @GetMapping({"signup.html", "signup"})
    public String signUp(Model model){
        Form form = formRepository.getForm(new Usuario());
        model.addAttribute("form", form);
        return "user/signup";
    }
}
