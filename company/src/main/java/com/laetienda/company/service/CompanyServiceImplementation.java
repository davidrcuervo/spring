package com.laetienda.company.service;

import com.laetienda.company.repository.CompanyRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiUser;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImplementation implements CompanyService{
    private final static Logger log = LoggerFactory.getLogger(CompanyServiceImplementation.class);

    @Autowired private CompanyRepository repo;
    @Autowired private ApiUser apiUser;
    @Autowired private ApiSchema apiSchema;

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
                Company result = repo.create(company);
                Member member = new Member(result, userId, CompanyMemberStatus.ACCEPTED);
                repo.addMember(member);
                return result;

            }else{
                throw e;
            }
        }
    }

    @Override
    public Long isCompanyValid(String companyId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::companyId. $companyId: {}", companyId);

        try {
            Long id = Long.parseLong(companyId);
            return repo.isCompanyValid(id);
        }catch(NumberFormatException e){
            String message = String.format("COMPANY_SERVICE::isCompanyValid. companyId must be number of Long format. $companyId: %s", companyId);
            log.warn(message);
            throw new NotValidCustomException(e);
        }
    }

    @Override
    public Company find(String strId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::find. $id: {}", strId);
        return repo.find(isCompanyValid(strId));
    }

    @Override
    public Company findByName(String name) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findByName. $name: {}", name);
        return repo.findByName(name);
    }

    @Override
    public void delete(String companyId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::delete. $id: {}", companyId);
        Long cid = isCompanyValid(companyId);
        List<Member> members = findAllMembers(cid);

        for(Member member : members){
            removeMember(cid, member.getUserId());
        }

        repo.deleteById(cid);
    }

    @Override
    public Company removeMember(Long companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::removeMember. $company: {} | $userId: {}", companyId, userId);

        Member member = findMemberByIds(companyId.toString(), userId);
        return repo.removeMember(member);
    }

    @Override
    public Member addMember(String companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::addMember. $company: {} | $userId: {}", companyId, userId);

        Long cid = isCompanyValid(companyId);
        String uid = apiUser.isUserIdValid(userId);
        Company company = repo.findNoJwt(cid);
        CompanyMemberPolicy policy = company.getMemberPolicy();
        CompanyMemberStatus status = CompanyMemberStatus.REQUESTED;

        if(policy == CompanyMemberPolicy.PUBLIC || policy == CompanyMemberPolicy.REGISTRATION_REQUIRED) {
            status = CompanyMemberStatus.ACCEPTED;
        }

        Member member = new Member(company, uid, status);
        company.addMember(member);

        return repo.addMember(member);
    }

    @Override
    public Member findMemberByIds(String companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findMemberByIds. $companyid: {} | $userId: {}", companyId, userId);

        String uid = apiUser.isUserIdValid(userId);
        Long cid = isCompanyValid(companyId);

        List<Member> result = repo.findMemberByUserId(cid, uid);

        if (result == null || result.isEmpty()) {
            String message = String.format("COMPANY_SERVICE::findMemberByIds. User is not member of company. $companyId: %d | $user: '%s'd", cid, uid);
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "member");

        } else if (result.size() > 1) {
            String message = String.format("COMPANY_SERVICE::findMemberByIds. There are more than one member in company with same userId. $companyId: %s | $user: %s", cid, uid);
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "member");
        }

        return result.getFirst();
    }

    @Override
    public List<Member> findAllMembers(Long cid) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findAllMembers. $cid: {}", cid);

        return repo.findAllMembers(cid);
    }
}
