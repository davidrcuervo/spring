package com.laetienda.company.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;

public interface CompanyService {
    Company create(Company company) throws NotValidCustomException;
    Company find(String id) throws NotValidCustomException;
    Company findByName(String name) throws NotValidCustomException;
    void delete(String idStr) throws NotValidCustomException;
}
