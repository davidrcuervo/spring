package com.laetienda.frontend.service;

import com.laetienda.frontend.model.Feedback;
import com.laetienda.lib.interfaces.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import jakarta.validation.Valid;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Set;

public interface FrontendCompanyService {

    boolean isCompanyTestTemplateEnabled();

    List<InputOptions> getAllCompanyMemberPolicies();
    String href(String vanityUrl);
    Company create(@Valid Company company, BindingResult bindingResult);
    List<Company> getAll();
    List<Company> getManagedCompanies();
    Company getCompanyByVanityUrl(String vanityUrl);
    Set<InputOptions> getMembers(Company company);
    Set<InputOptions> getManagers(Company company);
    Set<InputOptions> getTest(Company company);
    boolean isManager(Company company, String userId);
    boolean isAccepted(Company company, String userId);

    Feedback updateField(String vanityUrl, String field, MultiValueMap<String, String> params);
    Feedback updateMember(String vanityUrl, String role, String userId, MultiValueMap<String, String> params);
}
