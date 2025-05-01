package com.laetienda.frontend.controller;

import com.laetienda.lib.exception.CustomRestClientException;
import com.laetienda.frontend.model.Form;
import com.laetienda.frontend.repository.FormRepository;
import com.laetienda.utils.service.RestClientService;
import com.laetienda.lib.model.Mistake;
import com.laetienda.lib.options.HtmlFormAction;
import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.model.user.Usuario;
import com.laetienda.model.user.UsuarioList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.client.RestClient;

import java.security.Principal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("manage")
public class ManagementController {
    final private static Logger log = LoggerFactory.getLogger(ManagementController.class);
    private final RestClient client;

    @Value("${api.usuario.port}") private String usuarioPort;

    @Value("${api.user.findall}")
    private String urlFindAllUsers;

    @Value("${api.user.remove}")
    private String urlRemoveUser;

    @Value("${api.group.findall}")
    private String urlFindAllGroups;

    @Value("${api.group.find}")
    private String urlFindGroup;

    @Value("${api.group.update}")
    private String urlUpdateGroup;

    @Value("${api.group.remove.member}")
    private String urlGroupRemoveMember;

    @Value("${api.group.remove.owner}")
    private String urlGroupRemoveOwner;

    @Value("${api.group.add.member}")
    private String urlGroupAddMember;

    @Value("${api.group.add.owner}")
    private String urlGroupAddOwner;

    @Autowired private RestClientService restclient;
    @Autowired private FormRepository formRepository;
    @Autowired private Environment env;

    public ManagementController(RestClient client){
        this.client = client;
    }

    @GetMapping("/{viewpath}")
    public String getView(@PathVariable String viewpath, Model model, HttpSession session){
        log.trace("$viewpath: {}", viewpath);
        model.addAttribute("activemainmenu", "userandsettings");
        model.addAttribute("activesidemenu",viewpath);
        model.addAttribute("sessionId", session.getId());
        model.addAttribute("encodedSessionId", Base64.getEncoder().encodeToString(session.getId().getBytes()));
        return "management/" + viewpath;
    }

    @GetMapping("users.html")
    public String getUsers(Model model, HttpSession session){
        log.trace("Running manage users controller.");
        Map<String, Usuario> users = restclient.send(urlFindAllUsers, HttpMethod.GET, null, UsuarioList.class, null).getUsers();
        model.addAttribute("users", users);
        return getView("users.html", model, session);
    }

    @RequestMapping (value="users.html", method=RequestMethod.POST)
    public String postUserRequest(@RequestParam Map<String, String> params, Model model, HttpSession session){
        switch(params.get("action")){
            case("DELETE"):
                return deleteUser(params.get("username"), model, session);
            default:
                return getUsers(model, session);
        }
    }

    private String deleteUser(@RequestParam String username, Model model, HttpSession session){
        log.trace("Running delete user controller. $username: {}", username);
        String address = urlRemoveUser.replaceFirst("\\{username\\}", username);

        try {
            restclient.delete(address, Boolean.class);
            return "redirect:/manage/users.html";

        }catch(CustomRestClientException e){
            Map<String, Mistake> postErrors = new HashMap<>();
            postErrors.put(username, e.getMistake());
            model.addAttribute("postErrors", postErrors);
            return getUsers(model, session);
        }
    }

    @GetMapping("groups.html")
    public String getGroups(Model model, HttpSession session){
        log.trace("Running getGroups (/manage/groups.html) controller");
        GroupList groups = restclient.findall(urlFindAllGroups, GroupList.class);
        model.addAttribute("groups", groups.getGroups());

        return getView("groups.html", model, session);
    }

    @GetMapping("group.html")
    public String getGroup(@RequestParam Map<String, String> params, Model model, HttpSession session){
        log.trace("Running getGroup controller. $Group: {}", params.get("groupname"));
        Group group = restclient.find(urlFindGroup, Group.class, params);
        Form form = formRepository.getForm(group);
        form.setAction(HtmlFormAction.UPDATE);
        model.addAttribute("group", group);
        model.addAttribute("form", form);
        return getView("group.html", model, session);
    }

    @PostMapping("group.html")
    public String postGroupRequest(@RequestParam Map<String, String> params, Model model, HttpSession session) throws HttpClientErrorException {
        log.trace("Running postGroupRequest. $name: {}, $action: {}, $gname: {}, $username: {}", params.get("name"), params.get("action"), params.get("gname"), params.get("username"));
        switch (params.get("action")){
            case "removemember":
                return addAndRemoveOwnersAndMembersFromGroup(params, model, urlGroupRemoveMember, HttpMethod.DELETE, null, session);
            case "removeowner":
                return addAndRemoveOwnersAndMembersFromGroup(params, model, urlGroupRemoveOwner, HttpMethod.DELETE, null, session);
            case "addmember":
                return addAndRemoveOwnersAndMembersFromGroup(params, model, urlGroupAddMember, HttpMethod.PUT, null, session);
            case "addowner":
                return addAndRemoveOwnersAndMembersFromGroup(params, model, urlGroupAddOwner, HttpMethod.PUT, null, session);
            case "UPDATE":
                Group data = restclient.find(urlFindGroup, Group.class, params);
                data.setName(params.get("name"));
                data.setDescription(params.get("description"));
                return addAndRemoveOwnersAndMembersFromGroup(params, model, urlUpdateGroup, HttpMethod.PUT, data, session);
            default:
                return "redirect:/notfound.html";
        }
    }

    @GetMapping("${api.frontend.manger.test.file}")//manage/test.html
    public String test(Model model, Principal principal){
        log.trace("MANAGEMENT_CONTROLLER::test. $username: {}", principal.getName());

        if(principal instanceof OAuth2AuthenticationToken authentication){
            log.trace("MANAGEMENT_CONTROLLER::test $principal.name: {}", principal.getName());
            log.trace("MANAGEMENT_CONTROLLER::test $principal.email: {}", authentication.getPrincipal().getAttribute("email").toString());
            log.trace("MANAGEMENT_CONTROLLER::test $principal.family_nome: {}", authentication.getPrincipal().getAttribute("family_name").toString());
            log.trace("MANAGEMENT_CONTROLLER::test $principal.given_name: {}", authentication.getPrincipal().getAttribute("given_name").toString());

//            Map<String, Object> attributes = authentication.getPrincipal().getAttributes();
//            for(Map.Entry<String, Object> attribute : attributes.entrySet()){
//                log.trace("MANAGEMENT_CONTROLLER::test $attribute: {}", attribute.getKey());
//            }

        }else {
            log.trace("MANAGEMENT_CONTROLLER::test (non-OAuth2): {}", principal.getName());
        }

        //Send request to User Resource server
        String address = env.getProperty("api.usuario.testAuthorization.uri"); //http://usuarioet:{port}/api/v0/user/testAuthorization.html
        log.trace("MANAGEMENT_CONTROLLER::test $address: {}", address);
        String result = client.get()
                .uri(address, usuarioPort)
//                .attributes(clientRegistrationId("keycloak"))
                .retrieve()
                .toEntity(String.class).getBody();
        model.addAttribute("result", result);
        return "user/test";
    }

    private String addAndRemoveOwnersAndMembersFromGroup(Map<String, String> params, Model model, String url, HttpMethod method, Group data, HttpSession session) {
        Map<String, List<String>> errors = new HashMap<>();
        try{
            Group group = restclient.send(url, method, data, Group.class, params);
            return String.format("redirect:/manage/group.html?groupname=%s", group.getName());
        }catch(CustomRestClientException e){
            errors = e.getMistake().getErrors();

            errors.forEach((key, list) -> {
                log.trace("$error title: {}", key);
                list.forEach((error -> {
                    log.trace("$error: {}");
                }));
            });

            model.addAttribute("errors", errors);
            return getGroup(params, model, session);
        }
    }
}
