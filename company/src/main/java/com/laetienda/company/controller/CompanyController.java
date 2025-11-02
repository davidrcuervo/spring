package com.laetienda.company.controller;

import com.laetienda.company.service.CompanyService;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.company.folder}") //api/v0/Company
public class CompanyController {
    private final static Logger log = LoggerFactory.getLogger(CompanyController.class);

    @Autowired
    private CompanyService service;

    @PostMapping("${api.company.create.file}") //api/v0/Company/create
    public ResponseEntity<Company> create(@RequestBody @Valid Company company) throws NotValidCustomException {
        log.debug("COMPANY_CONTROLLER::create. $company: {}", company.getName());
        return ResponseEntity.ok(service.create(company));
    }

    @GetMapping("${api.company.find.file}") //api/v0/company/find/{id}
    public ResponseEntity<Company> find(@PathVariable String id) throws NotValidCustomException {
        log.debug("COMPANY_CONTROLLER::find. $id: {}", id);
        return ResponseEntity.ok(service.find(id));
    }

    @GetMapping("${api.company.findByName.file}") //api/v0/company/findByName/{name}
    public ResponseEntity<Company> findByName(@PathVariable String name) throws NotValidCustomException {
        log.debug("COMPANY_CONTROLLER::findByName. $name: {}", name);
        return ResponseEntity.ok(service.findByName(name));
    }

    @DeleteMapping("${api.company.delete.file}") //api/v0/company/delete/{id}
    public ResponseEntity delete(@PathVariable String id) throws NotValidCustomException {
        log.debug("COMPANY_CONTROLLER::delete $id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
