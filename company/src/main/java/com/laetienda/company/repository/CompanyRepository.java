package com.laetienda.company.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;

public interface CompanyRepository {
    Company create(Company company) throws NotValidCustomException;
    Company findByName(String name) throws NotValidCustomException;
    Company find(Long id) throws NotValidCustomException;
    Company findNoJwt(Long id) throws NotValidCustomException;
    void deleteById(Long id) throws NotValidCustomException;
    Company removeMember(Member member) throws NotValidCustomException;
    Member findMemberByUserId(String companyName, String userId) throws NotValidCustomException;
    Member addMember(Member member) throws NotValidCustomException;
    Long isCompanyValid(Long id) throws NotValidCustomException;
}
