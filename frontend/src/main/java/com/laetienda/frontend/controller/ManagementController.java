package com.laetienda.frontend.controller;

import com.laetienda.frontend.lib.CustomRestClientException;
import com.laetienda.frontend.service.RestClientService;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.model.Mistake;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("manage")
public class ManagementController {
    final private static Logger log = LoggerFactory.getLogger(ManagementController.class);

    @Value("${api.user.findall}")
    private String urlFindAllUsers;

    @Value("${api.user.remove}")
    private String urlRemoveUser;

    @Value("${api.group.findall}")
    private String urlFindAllGroups;

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
        log.trace("Running manage users controller.");
        Map<String, Usuario> users = restclient.findall(urlFindAllUsers, UsuarioList.class).getUsers();
        model.addAttribute("users", users);
        return getView("users.html", model);
    }

    @PostMapping("users.html")
    public String postUserRequest(@RequestParam Map<String, String> params, Model model){
        switch(params.get("action")){
            case("DELETE"):
                return deleteUser(params.get("username"), model);
            default:
                return getUsers(model);
        }
    }

    private String deleteUser(@RequestParam String username, Model model){
        log.trace("Running delete user controller. $username: {}", username);
        String address = urlRemoveUser.replaceFirst("\\{username\\}", username);

        try {
            restclient.delete(address, Boolean.class);
            return "redirect:/manage/users.html";

        }catch(CustomRestClientException e){
            Map<String, Mistake> postErrors = new HashMap<>();
            postErrors.put(username, e.getMistake());
            model.addAttribute("postErrors", postErrors);
            return getUsers(model);
        }
    }

    @GetMapping("groups.html")
    public String getGroups(Model model){
        log.trace("Running getGroups (/manage/groups.html) controller");
        GroupList groups = restclient.findall(urlFindAllGroups, GroupList.class);
        model.addAttribute("groups", groups.getGroups());

        return getView("groups.html", model);
    }
}
