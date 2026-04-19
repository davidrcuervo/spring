package com.laetienda.utils.service.api;

import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

public interface ApiCompany extends ApiRestClient{

    Company find(Long id)  throws HttpStatusCodeException;
    Company find(Long id, String token) throws HttpStatusCodeException;
    Company createCompany(Company company) throws HttpStatusCodeException;
    Company createCompany(Company company, String token) throws HttpStatusCodeException;
    void deleteCompany(String companyId) throws HttpStatusCodeException;
    void deleteCompany(Long companyId, String token) throws HttpStatusCodeException;

    String isValid(Long id, String token) throws HttpStatusCodeException;

    Company findByName(String name, String token) throws HttpStatusCodeException;

    Company updateName(Long id, String newName, String token) throws HttpStatusCodeException;

    Company updateDescription(Long id, String description, String token) throws HttpStatusCodeException;

    Company updateContent(Long id, Map<String, String> body, String token) throws HttpStatusCodeException;

    Member addMember(Long companyId, String userId, String token) throws  HttpStatusCodeException;

    Member findMember(Long companyId, String userId, String token) throws HttpStatusCodeException;

    Member updateMember(Member member, String token) throws HttpStatusCodeException;

    void deleteMember(Long companyId, String userId, String token) throws HttpStatusCodeException;
}
