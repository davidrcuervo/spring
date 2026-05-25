package com.laetienda.frontend.service;

import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

public interface FrontendCompanyService {

    List<InputOptions> getAllCompanyMemberPolicies();
    String href(String vanityUrl);
    Company create(@Valid Company company, BindingResult bindingResult);
    List<Company> getAll();
}
