package com.laetienda.frontend.controller;

import com.laetienda.frontend.model.ThankyouPage;
import com.laetienda.frontend.service.ThankYouPageService;
import com.laetienda.model.company.Company;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.server.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

@Controller
//@RequestMapping("${seo.thankYou.folder}")
public class ThankYouController {
    final private static Logger log = LoggerFactory.getLogger(ThankYouController.class);

//    final private ThankYouPageService service;
//
//    public ThankYouController(ThankYouPageService thankYouPageService) {
//        this.service = thankYouPageService;
//    }

//    @GetMapping("/**")
//    public String getPage(Model model, HttpServletRequest request){
//      log.debug("thankyou.key: {}", request.getServletPath());
//
//        ThankyouPage result = service.get(request.getServletPath());
//        if(result == null){
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
//        }
//
//        model.addAttribute("thankYou", result);
//
//        return "thankYou/thankYou.html";
//    }

    @GetMapping("${seo.thankYou.company.create.path}")
    public String companyCreate(
            @RequestParam(required = true) String element,
            HttpServletRequest request,
            HttpSession session
    ){
        log.debug("CONTROLLER_THANK_YOU::companyCreate | $element: {}", element);

        try {
            Company company = (Company) session.getAttribute(element);
            session.removeAttribute(element);
            request.setAttribute("company", company);
            return "thankYou/company/create.html";

        }catch(IllegalStateException | ClassCastException e){
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
        }
    }
}

