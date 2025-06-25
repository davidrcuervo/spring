package com.laetienda.company.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;

public interface CompanyRepository {
    Company create(Company company) throws NotValidCustomException;
    Company findByName(String name) throws NotValidCustomException;
    Company find(Long id) throws NotValidCustomException;
}
