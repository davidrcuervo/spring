package com.laetienda.company.repository;

import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CompanyRepository {
    Company create(Company company) throws HttpStatusCodeException;
    Company findByName(String name) throws HttpStatusCodeException;
    Company findByNameNoJwt(String name) throws HttpStatusCodeException;
    Long isCompanyValid(Long id) throws HttpStatusCodeException;
    Company find(Long id) throws HttpStatusCodeException;
    Company findNoJwt(Long id) throws HttpStatusCodeException;
    Company findByVanityUrl(String vanityUrl) throws HttpStatusCodeException;
    List<Company> findAll(Map<String, String> params) throws HttpStatusCodeException;
    List<Member> getAllManagers(Company company) throws HttpStatusCodeException;
    Company addManager(Member member) throws HttpStatusCodeException;
    void updateCompanyOwner(Member member) throws HttpStatusCodeException;
    void delete(Company company) throws HttpStatusCodeException;
    List<Member> getAllMembers(Long cid, Map<String, String> params) throws HttpStatusCodeException;
    List<Member> findMemberByUserId(Long companyId, String userId) throws HttpStatusCodeException;
    List<Member> findMemberByUserIdNoJwt(Long cid, String userId) throws HttpStatusCodeException;
    Member findMemberById(Long memberId) throws HttpStatusCodeException;
    Member addMember(Member member) throws HttpStatusCodeException;
    void acceptMember(Member member) throws HttpStatusCodeException;
    Member updateMember(Member member) throws HttpStatusCodeException;
    void removeMember(Member member) throws HttpStatusCodeException;
    Company updateCompany(Company company) throws HttpStatusCodeException;
    Company updateCompanyName(String newName, Company company) throws HttpStatusCodeException;

}
