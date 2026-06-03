package com.laetienda.webapp_test.repository;

import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface TestCompanyRepo {
//    public void cycle(TestUserDto[] users) throws HttpStatusCodeException, AssertionError;

    Company create(String name, String vanityUrl, CompanyMemberPolicy companyMemberPolicy, TestUserDto user) throws HttpStatusCodeException, AssertionError;
    String isValid(Long id, String token) throws HttpStatusCodeException, AssertionError;
    Company find(Long id, String token) throws HttpStatusCodeException, AssertionError;
    List<Company> findAll(Map<String, String> params, String token) throws HttpStatusCodeException, AssertionError;
    Company findByName(String name, String token) throws HttpStatusCodeException, AssertionError;
    Company findByVanityUrl(String vanityUrl, String token) throws HttpStatusCodeException;
    Company updateName(Long id, String newName, String token) throws HttpStatusCodeException, AssertionError;
    Company updateDescription(Long id, String description, String token) throws HttpStatusCodeException, AssertionError;
    Company updateContent(Long id, Map<String, String> body, String token) throws HttpStatusCodeException, AssertionError;
    void delete(Long id, String token) throws HttpStatusCodeException, AssertionError;
    List<Member> getMembers(long companyId, Map<String, String> params, String token) throws HttpStatusCodeException, AssertionError;
    Member addMember(Long id, String userId, String token) throws  HttpStatusCodeException, AssertionError;
    Member findMember(Long id, String userId, String token) throws HttpStatusCodeException, AssertionError;
    Member updateMember(Member memb1, String token) throws HttpStatusCodeException, AssertionError;
    void deleteMember(Long id, String userId, String token) throws HttpStatusCodeException, AssertionError;
    Company addManager(long companyId, String userId, String token) throws HttpStatusCodeException, AssertionError;
    List<Member> getManagers(Long id, String token) throws HttpStatusCodeException, AssertionError;
    void getAllCompanyMemberPolicies(String token) throws HttpStatusCodeException, AssertionError;
}
