package com.laetienda.frontend.controller;

import com.laetienda.frontend.model.ThankyouPage;
import com.laetienda.frontend.service.ThankyouPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("thankyou")
public class ThankyouController {

    final private static Logger log = LoggerFactory.getLogger(ThankyouController.class);
    @Autowired
    private ThankyouPageService service;

    @GetMapping("/**")
    public String getPage(Model model, HttpServletRequest request){
//        log.debug("thankyou.key: {}", request.getServletPath());

        ThankyouPage result = service.get(request.getServletPath());
        if(result == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("thankyou", result);

        return "thankyou/thankyou.html";
    }
}

