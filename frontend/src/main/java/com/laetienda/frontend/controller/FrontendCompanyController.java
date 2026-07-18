package com.laetienda.frontend.controller;

import com.laetienda.frontend.model.Feedback;
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
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Controller
public class FrontendCompanyController {
    private final static Logger log = LoggerFactory.getLogger(FrontendCompanyController.class);

    @Value("${seo.manage.company.create}")
    private String companyCreateUri;

    @Value("${seo.manage.company.find}")
    private String manageCompanyUri;

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
    public String getManageCompany(
            @PathVariable String vanityUrl,
            Model model,
            HttpSession session
    ){
        log.debug("CONTROLLER_MANAGE_COMPANY::getManageCompany | $vanityUrl: {}", vanityUrl);

        Company company = service.getCompanyByVanityUrl(vanityUrl);
        model.addAttribute("company", company);

        setFeedback(session, model);

        return "manage/company/manageCompany.html";
    }

    @PostMapping("${seo.manage.company.update.pattern}") //manage/company/{vanityUrl}/update/{field}.do
    public String postManageCompany(
            @PathVariable String vanityUrl,
            @PathVariable String field,
            @RequestParam MultiValueMap<String, String> params,
            HttpSession session
    ){
        log.debug("CONTROLLER_MANAGE_COMPANY::postManageCompany | $field: {} | $vanityUrl: {}", field, vanityUrl);

        Feedback feedback = service.updateField(vanityUrl, field, params);

        session.setAttribute("feedback", feedback);
        String redirect = tb.setAddressParams(null, manageCompanyUri, vanityUrl);
        return String.format("redirect:%s", redirect);
    }

    @PostMapping("${seo.manage.company.update.member}")
    public String postManageMemberOfCompany(
            @PathVariable String vanityUrl,
            @PathVariable String role,
            @PathVariable String userId,
            @RequestParam MultiValueMap<String, String> params,
            HttpSession session
    ){
        log.debug("CONTROLLER_COMPANY::postManageMemberOfCompany | $vanityUrl: {} | $role: {} | $userId: {}", vanityUrl, role, userId);

        Feedback feedback = service.updateMember(vanityUrl, role, userId, params);

        session.setAttribute("feedback", feedback);
        String redirect = tb.setAddressParams(null, manageCompanyUri, vanityUrl);
        return String.format("redirect:%s", redirect);
    }

    private void setFeedback(HttpSession session, Model model) {

        if(session.getAttribute("feedback") == null) {
            model.addAttribute("feedback", new Feedback());
        }else{
            Feedback feedback = (Feedback) session.getAttribute("feedback");
            session.removeAttribute("feedback");
            model.addAttribute("feedback", feedback);
        }
    }
}
