package com.laetienda.company.service;

import com.laetienda.company.repository.CompanyRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.service.api.ApiUser;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceImplementation implements CompanyService{
    private final static Logger log = LoggerFactory.getLogger(CompanyServiceImplementation.class);

    @Autowired private CompanyRepository repo;
    @Autowired private ApiUser apiUser;

    @Override
    public Company create(@NotNull Company company) throws NotValidCustomException {
        String userId = apiUser.getCurrentUserId();
        log.debug("COMPANY_SERVICE::create. $company: {}. $currentUserId: {}", company.getName(), userId);

        try {
            Company temp = repo.findByName(company.getName());
            String message = String.format("Company %s already exists.", company.getName());
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "company");

        }catch(NotValidCustomException e){
            if(e.getStatus() == HttpStatus.NOT_FOUND){
                company.addMember(new Member(company, userId, userId));
                return repo.create(company);
            }else{
                throw e;
            }
        }
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

    @Override
    public void delete(String idStr) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::delete. $id: {}", idStr);

        try {
            Long id = Long.parseLong(idStr);
            repo.deleteById(id);

        } catch (NumberFormatException n){
            String message = String.format("Company id is not valid. $id: %s. EXCEPTION: %s", idStr, n.getMessage());
            log.trace(message, n);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "company");
        }
    }
}
