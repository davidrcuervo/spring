package com.laetienda.company.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;

import java.util.List;

public interface CompanyService {
    Company create(Company company) throws NotValidCustomException;
    Long isCompanyValid(String companyId) throws NotValidCustomException;
    Company find(String id) throws NotValidCustomException;
    Company findByName(String name) throws NotValidCustomException;
    void delete(String idStr) throws NotValidCustomException;
    Company removeMember(Long companyId, String userId) throws NotValidCustomException;
    Member addMember(String companyName, String userId) throws NotValidCustomException;
    Member findMemberByIds(String companyId, String userId) throws NotValidCustomException;
    List<Member> findAllMembers(Long cid) throws NotValidCustomException;
}
