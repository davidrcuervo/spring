package com.laetienda.company.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.utils.service.api.ApiSchema;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Repository
public class CompanyRepositoryImplementation implements CompanyRepository{
    private final static Logger log = LoggerFactory.getLogger(CompanyRepositoryImplementation.class);

    private final RestClient client;
    @Autowired private ApiSchema schema;
    @Autowired private Environment env;
    @Autowired private ObjectMapper json;

    @Value("${kc.client-id}") String webappClientId;

    public CompanyRepositoryImplementation(RestClient restClient){
        this.client= restClient;
    }

    @Override
    public Company create(@NotNull Company company) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::create. $company: {}", company.getName());
        return schema.create(Company.class, company).getBody();
    }

    @Override
    public Long isCompanyValid(Long id) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::isCompanyValid. $companyId: {}", id);
        String companyId = schema.isItemValid(Company.class, id).getBody();

        try{
            return Long.parseLong(companyId);
        } catch (NumberFormatException e) {
            String message = String.format("COMPANY_REPOSITORY::isCompanyValid. Invalid long id format. $id: %s | $error: %s", companyId, e.getMessage());
            log.error(message);
            log.trace(message, e);
            throw new NotValidCustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "company");
        }
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
    public Company findNoJwt(Long id) throws NotValidCustomException {
        log.debug("COMPANY_REPO::findNoJwt. $id: {}", id);

        String address = env.getProperty("api.schema.findById.uri", "findById");
        String clazzName = schema.getClazzName(Company.class);
        try{
            return client.get().uri(address, id.toString(), clazzName)
                    .accept(MediaType.APPLICATION_JSON)
                    .attributes(clientRegistrationId(webappClientId))
                    .retrieve().toEntity(Company.class).getBody();
        }catch(Exception e){
            throw new NotValidCustomException(e);
        }
    }

    @Override
    public void deleteById(Long id) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::deleteById. $id: {}", id);
        schema.deleteById(Company.class, id);
    }

    @Override
    public List<Member> findMemberByUserId(Long companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_REPO::findMemberByUserId. $companyId: {} | $user: {}", companyId, userId);
        String query = String.format("SELECT m FROM %s m INNER JOIN m.company c WHERE c.id = %d AND m.userId = '%s'", Member.class.getName(), companyId, userId);
        return findMembersByQuery(query);
    }

    @Override
    public List<Member> findAllMembers(Long cid) throws NotValidCustomException {
        log.debug("COMPANY_REPO::findAllMembers. $cid: {}", cid);
        String query = String.format("SELECT m FROM %s m INNER JOIN m.company c WHERE c.id = %d", Member.class.getName(), cid);
        return findMembersByQuery(query);
    }

    private List<Member> findMembersByQuery(String query) throws NotValidCustomException{
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);

        ResponseEntity<String> response = schema.findByQuery(Member.class, params);
        log.trace("COMPANY_REPO::findMemberByUserId. $response: {}", response.getBody());

        try {
            return json.readValue(response.getBody(), new TypeReference<List<Member>>() {});
        } catch (JsonProcessingException e) {
            String message = String.format("COMPANY_REPO::findMemberByUserId. $error: %s", e.getMessage());
            log.error(message);
            log.trace(message, e);
            throw new NotValidCustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "company");
        }
    }

    @Override
    public Company removeMember(Member member) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::removeMember. $company: {}, $user: {}", member.getCompany().getName(), member.getUserId());

        schema.deleteById(Member.class, member.getId());
        return find(member.getCompany().getId());
    }

    @Override
    public Member addMember(Member member) throws NotValidCustomException {
        log.debug("COMPANY_REPOSITORY::addMember. $company: {}, $user: {}", member.getCompany().getName(), member.getUserId());

        return schema.create(Member.class, member).getBody();
    }
}
