package com.laetienda.company.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.utils.service.api.ApiSchema;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class CompanyRepositoryImplementation implements CompanyRepository{
    private final static Logger log = LoggerFactory.getLogger(CompanyRepositoryImplementation.class);

    @Autowired private ApiSchema schema;

    @Override
    public Company create(@NotNull Company company) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::create. $company: {}", company.getName());
        return schema.create(Company.class, company).getBody();
    }

    @Override
    public Company findByName(String name) throws NotValidCustomException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("name", name);
        return schema.find(Company.class, body).getBody();
    }

    @Override
    public Company find(Long id) throws NotValidCustomException {
        return schema.findById(Company.class, id).getBody();
    }

    @Override
    public void deleteById(Long id) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::deleteById. $id: {}", id);
        schema.deleteById(Company.class, id);
    }
}
