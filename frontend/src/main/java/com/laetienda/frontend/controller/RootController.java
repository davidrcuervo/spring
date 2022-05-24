package com.laetienda.frontend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class RootController {

    final private static Logger log = LoggerFactory.getLogger(RootController.class);

    @GetMapping("/{viewpath}")
    public String getView(@PathVariable String viewpath){
        log.trace("Running getView controller in Root. $viewpath: {}", viewpath);
        return String.format("Root/%s", viewpath);
    }

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home(){
        return "Root/home";
    }
}
