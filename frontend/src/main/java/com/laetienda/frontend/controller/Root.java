package com.laetienda.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Root {

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home(){
        return "Root/home";
    }
}
