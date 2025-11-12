package com.laetienda.company.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;

import java.util.List;

public interface CompanyRepository {
    Company create(Company company) throws NotValidCustomException;
    Company findByName(String name) throws NotValidCustomException;
    Company findByNameNoJwt(String name) throws NotValidCustomException;
    Long isCompanyValid(Long id) throws NotValidCustomException;
    Company find(Long id) throws NotValidCustomException;
    Company findNoJwt(Long id) throws NotValidCustomException;
    void deleteById(Long id) throws NotValidCustomException;
    List<Member> findAllMembers(Long cid) throws NotValidCustomException;
    List<Member> findMemberByUserId(Long companyId, String userId) throws NotValidCustomException;
    List<Member> findMemberByUserIdNoJwt(Long cid, String userId) throws NotValidCustomException;
    Member addMember(Member member) throws NotValidCustomException;
    Company removeMember(Member member) throws NotValidCustomException;
    Member updateMember(Member member) throws NotValidCustomException;
    Company updateCompany(Company company) throws NotValidCustomException;

}
