package com.laetienda.frontend.service;

import com.laetienda.lib.options.InputOptions;
import com.laetienda.utils.service.api.ApiCompany;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("comp")
public class FrontendCompanyServiceImplementation implements FrontendCompanyService {
    private final static Logger log = LoggerFactory.getLogger(FrontendCompanyServiceImplementation.class);

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
}
