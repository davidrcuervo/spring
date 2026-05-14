package com.laetienda.frontend.controller;

import com.laetienda.model.kc.KcUser;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.thymeleaf.TemplateEngine;

@Controller
public class RootController {
    final private static Logger log = LoggerFactory.getLogger(RootController.class);

    final private ApplicationContext application;
    final private ApiUser apiUser;

    @Value("${seo.home.file}")
    private String homeUri;

    public RootController(
            ApplicationContext applicationContext,
            ApiUser apiUser
    ) {
        this.application = applicationContext;
        this.apiUser = apiUser;
    }

    @GetMapping("/{viewPath}")
    public String getView(@PathVariable String viewPath) throws HttpStatusCodeException {
        log.debug("ROOT_CONTROLLER::getView. $viewPath: {}", viewPath);

        String templateName = String.format("Root/%s", viewPath);
        String template = String.format("classpath:/templates/%s", templateName);

        Resource resource = application.getResource(template);

        if(!resource.exists()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "NOT FOUND: " + viewPath);
        }

        return templateName;
    }

    @GetMapping({"/", "home", "home.html", "index", "index.html"})
    public String home() {
        log.debug("ROOT_CONTROLLER::home.");
        return "Root/home";
    }

    @GetMapping("${seo.signIn}")
    public String signIn(@RequestParam(required = false) String redirect){
        log.debug("ROOT_CONTROLLER::signIn. | $redirect: {}", redirect);

        if(redirect != null && !redirect.isBlank()){
            return "redirect:" + redirect;
        }
        return String.format("redirect:%s", homeUri);
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon(){
        log.debug("ROOT_CONTROLLER::favicon.");
        return ResponseEntity.noContent().build();
    }
}
