package com.laetienda.frontend.controller.manage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendManageCompanyController {
    private final static Logger log = LoggerFactory.getLogger(FrontendManageCompanyController.class);

    @GetMapping("${seo.manage.company.create}")
    public String create(){
        log.debug("CONTROLLER_MANAGE_COMPANY::create");
        return "/manage/company/create";
    }
}
