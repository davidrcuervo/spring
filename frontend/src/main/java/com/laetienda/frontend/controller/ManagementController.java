package com.laetienda.frontend.controller;

import com.laetienda.frontend.service.RestClientService;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("manage")
public class ManagementController {
    final private static Logger log = LoggerFactory.getLogger(ManagementController.class);

    @Value("${api.user.findall}")
    private String urlFindAllUsers;

    @Autowired
    private RestClientService restclient;

    @GetMapping("/{viewpath}")
    public String getView(@PathVariable String viewpath, Model model){
        log.trace("$viewpath: {}", viewpath);
        model.addAttribute("activemainmenu", "userandsettings");
        model.addAttribute("activesidemenu",viewpath);
        return "management/" + viewpath;
    }

    @GetMapping("users.html")
    public String getUsers(Model model){
        log.trace("Runnig manage users controller.");
        Map<String, Usuario> users = restclient.findall(urlFindAllUsers, UsuarioList.class).getUsers();
        model.addAttribute("users", users);
        return getView("users.html", model);
    }
}
