package com.laetienda.company.service;

import com.laetienda.company.repository.CompanyRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImplementation implements CompanyService{
    private final static Logger log = LoggerFactory.getLogger(CompanyServiceImplementation.class);

    @Autowired private CompanyRepository repo;

    @Override
    public Company create(@NotNull Company company) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::create. $company: {}", company.getName());



        return repo.create(company);
    }

    @Override
    public Company find(String strId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::find. $id: {}", strId);
        try {
            Long id = Long.parseLong(strId);
            return repo.find(id);
        }catch(NumberFormatException e){
            String message = String.format("%s -> %s", e.getClass().getSimpleName(), e.getMessage());
            log.warn(message);
            throw new NotValidCustomException(e);
        }
    }

    @Override
    public Company findByName(String name) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findByName. $name: {}", name);
        return repo.findByName(name);
    }
}
