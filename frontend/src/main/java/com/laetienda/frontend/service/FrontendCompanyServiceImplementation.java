package com.laetienda.frontend.service;

import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.utils.service.api.ApiCompany;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("comp")
public class FrontendCompanyServiceImplementation implements FrontendCompanyService {
    private final static Logger log = LoggerFactory.getLogger(FrontendCompanyServiceImplementation.class);

    @Value("${seo.company.file}")
    private String companyUri;

    private final ApiCompany api;

    public FrontendCompanyServiceImplementation(ApiCompany apiCompany){
        this.api = apiCompany;
    }

    @Override
    public List<InputOptions> getAllCompanyMemberPolicies(){
        log.debug("SERVICE_FRONTEND_COMPANY::getAllCompanyMemberPolicies");

        try{
            return api.getAllCompanyMemberPolicies();
        }catch(Exception e){
            return new ArrayList<>();
        }
    }

    @Override
    public String href(String vanityUrl) {
        return UriComponentsBuilder.fromUriString(companyUri)
                .buildAndExpand(Map.of("vanityUrl", vanityUrl))
                .toUriString();
    }

    @Override
    public Company create(Company company, BindingResult bindingResult) {
        log.debug("SERVICE_COMPANY::create");

        if(bindingResult.hasErrors()){
            return null;
        }

        try{
            return api.createCompany(company);
        }catch(HttpStatusCodeException e){
            log.warn("SERVICE_COMPANY::create | $error: {} -> {}", e.getStatusCode(), e.getResponseBodyAsString());
            bindingResult.reject("error.global", e.getMessage());
            return null;
        }
    }

    @Override
    public List<Company> getAll() {
        log.debug("SERVICE_COMPANY::getAll");
        return api.findAll(Map.of("member", ""));
    }

    @Override
    public List<Company> getAllManaged(){
        log.debug("SERVICE_COMPANY::getAllManaged");
        return api.findAll(Map.of("manager", ""));
    }

    @Override
    public Company getCompanyByVanityUrl(String vanityUrl) {
        log.debug("SERVICE_COMPANY::getCompanyVanityUrl | $vanityUrl: {}", vanityUrl);
        return api.findByVanityUrl(vanityUrl);
    }
}
