package com.laetienda.utils.service.api;

import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface ApiCompany extends ApiRestClient{

    Company find(Long id)  throws HttpStatusCodeException;
    Company find(Long id, String token) throws HttpStatusCodeException;
    List<Company> findAll(Map<String, String> params) throws HttpStatusCodeException;

    List<Company> findAllWithToken(
            Map<String, String> params,
            String token
    ) throws HttpStatusCodeException;

    Company createCompany(Company company) throws HttpStatusCodeException;
    Company createCompany(Company company, String token) throws HttpStatusCodeException;
    void deleteCompany(String companyId) throws HttpStatusCodeException;
    void deleteCompany(Long companyId, String token) throws HttpStatusCodeException;
    String isValid(Long id, String token) throws HttpStatusCodeException;
    Company findByName(String name, String token) throws HttpStatusCodeException;
    Company findByVanityUrl(String vanityUrl) throws HttpStatusCodeException;

    Company findByVanityUrlWithToken(
            String vanityUrl,
            String token
    ) throws HttpStatusCodeException;

    Company updateName(Long id, String newName, String token) throws HttpStatusCodeException;
    Company updateDescription(Long id, String description, String token) throws HttpStatusCodeException;
    Company updateContent(Long id, Map<String, String> body, String token) throws HttpStatusCodeException;
    Member addMember(Long companyId, String userId, String token) throws  HttpStatusCodeException;
    Member findMember(Long companyId, String userId, String token) throws HttpStatusCodeException;
    Member updateMember(Member member, String token) throws HttpStatusCodeException;
    void deleteMember(Long companyId, String userId, String token) throws HttpStatusCodeException;
    Company addManager(long companyId, String userId) throws HttpStatusCodeException;
    Company addManagerWithToken(long companyId, String userId, String token) throws HttpStatusCodeException;

    List<InputOptions> getAllCompanyMemberPolicies() throws HttpStatusCodeException;

    List<InputOptions> getAllCompanyMemberPoliciesWithToken(
            String token
    ) throws HttpStatusCodeException;

    List<InputOptions> getAllCompanyMemberPoliciesWithClientRegistrationId(
            String clientRegistrationId
    ) throws HttpStatusCodeException;
}
