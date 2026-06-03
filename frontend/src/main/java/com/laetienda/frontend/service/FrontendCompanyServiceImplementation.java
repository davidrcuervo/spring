package com.laetienda.frontend.service;

import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.kc.KcUser;
import com.laetienda.utils.service.api.ApiCompany;
import com.laetienda.utils.service.api.ApiUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service("comp")
public class FrontendCompanyServiceImplementation implements FrontendCompanyService {
    private final static Logger log = LoggerFactory.getLogger(FrontendCompanyServiceImplementation.class);

    @Value("${seo.company.file}")
    private String companyUri;

    private final ApiUser apiUser;
    private final ApiCompany api;

    public FrontendCompanyServiceImplementation(
            ApiCompany apiCompany,
            ApiUser apiUser
    ){
        this.api = apiCompany;
        this.apiUser = apiUser;
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
    public List<Company> getManagedCompanies(){
        log.debug("SERVICE_COMPANY::getAllManaged");
        return api.findAll(Map.of("manager", ""));
    }

    @Override
    public Company getCompanyByVanityUrl(String vanityUrl) {
        log.debug("SERVICE_COMPANY::getCompanyVanityUrl | $vanityUrl: {}", vanityUrl);
        return api.findByVanityUrl(vanityUrl);
    }

    @Override
    public Set<InputOptions> getMembers(Company company) {
        log.debug("SERVICE_COMPANY::getMembers | $company: {}", company.getVanityUrl());

        try {
            List<Member> members = api.getMembers(company.getId(), null);

            Set<InputOptions> result = new HashSet<>();
            members.forEach(member -> {
                KcUser option = apiUser.getUserWithWebAppService(member.getUserId());
                result.add(option);
            });
            return result;
        }catch(HttpStatusCodeException e){
            log.error("SERVICE_COMPANY::getMembers | $error: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Set<InputOptions> getManagers(Company company){
        log.debug("SERVICE_COMPANY::getManagers | $company: {}", company.getVanityUrl());
        List<Member> managers = api.getManagers(company.getId());

        Set<InputOptions> result = new HashSet<>();
        managers.forEach(manager -> {
            KcUser option = apiUser.getUserWithWebAppService(manager.getUserId());
            result.add(option);
        });

        return result;
    }

    @Override
    public Set<InputOptions> getTest(Company company){
        Set<InputOptions> result = new HashSet<>();
        result.add(new InputOptions() {
            @Override
            public String getValue() {
                return "1";
            }

            @Override
            public String getLabel() {
                return "One";
            }

            @Override
            public String getDescription() {
                return "First one";
            }
        });
        result.add(new InputOptions() {
            @Override
            public String getValue() {
                return "2";
            }

            @Override
            public String getLabel() {
                return "Two";
            }

            @Override
            public String getDescription() {
                return "Second one";
            }
        });
        return result;
    }

    @Override
    public boolean isManager(Member member){
        log.debug("SERVICE_COMPANY::isManager | $memberId: {}", member.getId());
        return member.getCompany().getEditorGroups().stream().anyMatch(group ->
            group.getOwner().equals(member.getUserId()) ||
            group.getMembers().contains(member.getUserId())
        );
    }

    @Override
    public boolean isAccepted(Member member){
        log.debug("SERVICE_COMPANY::isAccepted | $memberId: {}", member.getId());
        return member.getStatus().equals(CompanyMemberStatus.ACCEPTED);
    }
}
