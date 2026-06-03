package com.laetienda.frontend.service;

import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Set;

public interface FrontendCompanyService {

    List<InputOptions> getAllCompanyMemberPolicies();
    String href(String vanityUrl);
    Company create(@Valid Company company, BindingResult bindingResult);
    List<Company> getAll();
    List<Company> getManagedCompanies();
    Company getCompanyByVanityUrl(String vanityUrl);
    Set<InputOptions> getMembers(Company company);
    Set<InputOptions> getManagers(Company company);
    Set<InputOptions> getTest(Company company);
    boolean isManager(Member member);
    boolean isAccepted(Member member);
}
