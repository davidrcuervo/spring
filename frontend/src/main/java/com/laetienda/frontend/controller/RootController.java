package com.laetienda.frontend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RootController {

    final private static Logger log = LoggerFactory.getLogger(RootController.class);

    @GetMapping("/{viewPath}")
    public String getView(@PathVariable String viewPath){
        log.debug("ROOT_CONTROLLER::getView. $viewpath: {}", viewPath);
        return String.format("Root/%s", viewPath);
    }

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home(){
        log.debug("ROOT_CONTROLLER::home.");
//        return "Welcome to the home page!";
        return "Root/home";
    }
}
