package com.laetienda.frontend.controller;

import com.laetienda.frontend.service.FrontendCompanyService;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.company.Company;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Controller
public class FrontendCompanyController {
    private final static Logger log = LoggerFactory.getLogger(FrontendCompanyController.class);

    @Value("${seo.manage.company.create}")
    private String companyCreateUri;

    @Value("${seo.thankYou.company.create.uri}")
    private String thankYouCompanyCreateUri;

    private final FrontendCompanyService service;
    private final ToolBoxService tb;

    public FrontendCompanyController(
            FrontendCompanyService frontendCompanyService,
            ToolBoxService toolBoxService
    ){
        this.service = frontendCompanyService;
        this.tb = toolBoxService;
    }

    @GetMapping("${seo.manage.company.create}")
    public String getCreateCompany(Model model){
        log.debug("CONTROLLER_MANAGE_COMPANY::create");
        Company company = new Company();
        model.addAttribute("company", company);
        return companyCreateUri;
    }

    @PostMapping("${seo.manage.company.create}")
    public String postCreateCompany(
            @ModelAttribute("company") @Valid Company company,
            BindingResult bindingResult,
            HttpSession session
    ){
        log.debug("CONTROLLER_MANAGE_COMPANY::postCreate");

        Company result = service.create(company, bindingResult);
        if(result == null || result.getId() <= 0) {
            return companyCreateUri;
        }

        String element = tb.newToken(32);
        session.setAttribute(element, result);

        //redirec:/thankYou/company/create.html?element={element}
        return "redirect:" + UriComponentsBuilder.fromUriString(thankYouCompanyCreateUri)
                .buildAndExpand(Map.of("element", element))
                .toUriString();
    }

    @GetMapping("${seo.manage.company.find}")
    public String getManageCompany(@PathVariable String vanityUrl, Model model){
        log.debug("CONTROLLER_MANAGE_COMPANY::getManageCompany");

        Company company = service.getCompanyByVanityUrl(vanityUrl);
        model.addAttribute("company", company);

        return "manage/company/manageCompany.html";
    }
}
