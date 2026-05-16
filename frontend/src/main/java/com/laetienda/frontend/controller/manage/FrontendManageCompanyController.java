package com.laetienda.frontend.controller.manage;

import com.laetienda.model.company.Company;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FrontendManageCompanyController {
    private final static Logger log = LoggerFactory.getLogger(FrontendManageCompanyController.class);

    @Value("${seo.manage.company.create}")
    private String createCompanyAddress;

//    private String create(Model model, Company company) {
//        model.addAttribute("company", company);
//        return createCompanyAddress;
//    }

    @GetMapping("${seo.manage.company.create}")
    public String create(Model model){
        log.debug("CONTROLLER_MANAGE_COMPANY::create");
        Company company = new Company();
        model.addAttribute("company", company);
        return createCompanyAddress;
    }

    @PostMapping("${seo.manage.company.create}")
    public String postCreate(
            @ModelAttribute("company") @Valid Company company,
            BindingResult bindingResult
    ){
        log.debug("CONTROLLER_MANAGE_COMPANY::postCreate");

        if(bindingResult.hasErrors()){
            return createCompanyAddress;
        }

        return String.format("redirect:%s", createCompanyAddress);
    }

}
