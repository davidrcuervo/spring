package com.laetienda.company.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaGroup;
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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Repository
public class CompanyRepositoryImplementation implements CompanyRepository{
    private final static Logger log = LoggerFactory.getLogger(CompanyRepositoryImplementation.class);

    private final RestClient client;
    private final ApiSchemaGroup apiSchemaGroup;

    @Autowired private ApiSchema schema;
    @Autowired private Environment env;
    @Autowired private ObjectMapper json;

    @Value("${kc.client-registration-id.webapp}")
    String webappClientId;

    public CompanyRepositoryImplementation(
            RestClient restClient,
            ApiSchemaGroup apiSchemaGroup
    ){
        this.client= restClient;
        this.apiSchemaGroup = apiSchemaGroup;
    }

    @Override
    public Company create(@NotNull Company company) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::create. $company: {}", company.getName());

        String managersGroupName = getManagersGroupName(company);
        DbGroup managers = new DbGroup(managersGroupName);
        managers.setUserAccessPolicy(DbUserAccessPolicy.MANAGE_BY_ALL);
        managers.setServiceAccessPolicy(DbServiceAccessPolicy.NO_SERVICE);
        company.addEditorGroup(managers);

        String readersGroupoName = getReadersGroupName(company);
        DbGroup readersGroup = new DbGroup(readersGroupoName);
        readersGroup.setUserAccessPolicy(DbUserAccessPolicy.MANAGE_BY_OWNER_ONLY);
        readersGroup.setServiceAccessPolicy(DbServiceAccessPolicy.SERVICE_WRITE);
        company.addReaderGroup(readersGroup);

        return schema.create(Company.class, company);
    }

    @Override
    public Long isCompanyValid(Long id) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::isCompanyValid. $companyId: {}", id);

        try{
            String companyId = schema.isItemValid(Company.class, id).getBody();
            return Long.parseLong(companyId);
        } catch (NumberFormatException e) {
            String message = String.format("COMPANY_REPOSITORY::isCompanyValid. Invalid long id format. $error: %s", e.getMessage());
            log.error("COMPANY_REPOSITORY::isCompanyValid. {}", message);
            log.trace(message, e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch(NotValidCustomException ce){
            throw ce.getHttpStatusCodeException();
        }
    }

    @Override
    public Company findByName(String name) throws HttpStatusCodeException {
        Map<String, String> body = new HashMap<String, String>();
        body.put("name", name);

        try {
            return schema.find(Company.class, body).getBody();
        } catch (NotValidCustomException e) {
            throw e.getHttpStatusCodeException();
        }
    }

    @Override
    public Company findByNameNoJwt(String name) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::findByNameNoJwt. $name: {}", name);
        String address = env.getProperty("api.schema.find.uri", "/api/v0/schema/find?clase={clazzName}");

        Map<String, String> body = new HashMap<String, String>();
        body.put("name", name);
        String clazzName = schema.getClazzName(Company.class);

        try {
            return client.post().uri(address, clazzName)
                    .attributes(clientRegistrationId(webappClientId))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(json.writeValueAsBytes(body))
                    .retrieve().toEntity(Company.class).getBody();
        }catch(HttpStatusCodeException e){
            throw e;

        }catch(Exception e){
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Company find(Long id) throws HttpStatusCodeException {

        return schema.findById(Company.class, id).getBody();
    }

    @Override
    public Company findNoJwt(Long id) throws HttpStatusCodeException {
        log.debug("COMPANY_REPO::findNoJwt. $id: {}", id);

        String address = env.getProperty("api.schema.findById.uri", "findById");
        String clazzName = schema.getClazzName(Company.class);
        return client.get().uri(address, id.toString(), clazzName)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(clientRegistrationId(webappClientId))
                .retrieve().toEntity(Company.class).getBody();
    }

    @Override
    public Company addManager(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::addManager. $member: {}", member.getId());

        DbGroup managers = getManagersGroup(member.getCompany());
        apiSchemaGroup.addMember(managers.getId(), member.getUserId());

        return find(member.getCompany().getId());
    }

    @Override
    public void updateCompanyOwner(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::updateCompanyOwner. $member: {}", member.getId());

        DbGroup managers = getManagersGroup(member.getCompany());
        DbGroup readers = getReadersGroup(member.getCompany());

        apiSchemaGroup.addMember(managers.getId(), managers.getOwner());
        apiSchemaGroup.addMember(readers.getId(), readers.getOwner());

        Map<String, String> body = Map.of("owner", member.getUserId());
        apiSchemaGroup.update(managers.getId(), body);
        apiSchemaGroup.update(readers.getId(), body);
    }

    @Override
    public void delete(Company company) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::delete. $id: {}", company.getId());

        DbGroup managers = getReadersGroup(company);
        DbGroup readers =  getManagersGroup(company);

        apiSchemaGroup.delete(managers.getId());
        apiSchemaGroup.delete(readers.getId());

        schema.deleteById(Company.class, company.getId());
    }

    @Override
    public List<Member> findMemberByUserId(Long cid, String userId) throws HttpStatusCodeException {
        log.debug("COMPANY_REPO::findMemberByUserId. $companyId: {} | $user: {}", cid, userId);
        String query = getQueryFindMemberByUserId(cid, userId);
        return findMembersByQuery(query);
    }

    @Override
    public List<Member> findMemberByUserIdNoJwt(Long cid, String userId) throws HttpStatusCodeException {
        log.debug("COMPANY_REPO::findMemberByUserIdNoJwt. $companyId: {} | $user: {}", cid, userId);
        String query = getQueryFindMemberByUserId(cid, userId);
        return findMembersByQueryNoJwt(query);
    }

    @Override
    public Member findMemberById(Long memberId) throws HttpStatusCodeException {
        log.debug("COMPANY_REPO::findMemberById. $id: {}", memberId);
        return schema.findById(Member.class, memberId).getBody();
    }

    private String getQueryFindMemberByUserId(Long cid, String userId) throws HttpStatusCodeException {
        return String.format("SELECT m FROM %s m INNER JOIN m.company c WHERE c.id = %d AND m.userId = '%s'", Member.class.getName(), cid, userId);
    }

    @Override
    public List<Member> findAllMembers(Long cid) throws HttpStatusCodeException {
        log.debug("COMPANY_REPO::findAllMembers. $cid: {}", cid);
        String query = String.format("SELECT m FROM %s m INNER JOIN m.company c WHERE c.id = %d", Member.class.getName(), cid);
        return findMembersByQuery(query);
    }

    private List<Member> findMembersByQuery(String query) throws HttpStatusCodeException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);

        ResponseEntity<String> response = null;

        try {
            response = schema.findByQuery(Member.class, params);
            return findMembersByQuery(response);
        } catch (NotValidCustomException e) {
            throw e.getHttpStatusCodeException();
        }
    }

    private List<Member> findMembersByQueryNoJwt(String query) throws HttpStatusCodeException{
        Map<String, String> params = new HashMap<String, String>();
        params.put("query", query);

        try {
            ResponseEntity<String> response = schema.findByQueryNoJwt(Member.class, params);
            return findMembersByQuery(response);
        } catch (NotValidCustomException e) {
            throw e.getHttpStatusCodeException();
        }
    }

    private List<Member> findMembersByQuery(ResponseEntity<String> response) throws NotValidCustomException {
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
    public void removeMember(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::removeMember. $company: {}, $user: {}", member.getCompany().getName(), member.getUserId());

        DbGroup managers = getManagersGroup(member.getCompany());
        DbGroup readers = getReadersGroup(member.getCompany());
        apiSchemaGroup.removeMember(readers.getId(), member.getUserId());

        //Remove member from managers if apply
        if(managers.getMembers().contains(member.getUserId())){
            apiSchemaGroup.removeMember(managers.getId(),  member.getUserId());
        }

        schema.deleteById(Member.class, member.getId());
    }

    @Override
    public Member updateMember(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::updateMember. $memberId: {}", member.getId());
        try {
            return schema.update(Member.class, member).getBody();
        } catch (NotValidCustomException e) {
            throw e.getHttpStatusCodeException();
        }
    }

    @Override
    public Company updateCompany(Company company) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::updateCompany. $company: {}", company.getName());

        try {
            return schema.update(Company.class, company).getBody();
        } catch (NotValidCustomException e) {
            throw e.getHttpStatusCodeException();
        }
    }

    @Override
    public Company updateCompanyName(String newName, Company company) throws HttpStatusCodeException{
        DbGroup managers = getManagersGroup(company);
        DbGroup readers = getReadersGroup(company);

        company.setName(newName);
        apiSchemaGroup.update(managers.getId(), Map.of("name", getManagersGroupName(company)));
        apiSchemaGroup.update(readers.getId(), Map.of("name", getReadersGroupName(company)));

        return this.updateCompany(company);
    }

    @Override
    public Member addMember(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::addMember. $company: {}, $user: {}", member.getCompany().getName(), member.getUserId());
        member.addEditorGroup(getManagersGroup(member.getCompany()));
        member.addEditor(member.getUserId());
        return schema.create(Member.class, member);
    }

    @Override
    public void acceptMember(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::acceptMember. $memberId: {}", member.getId());

        DbGroup readers = getReadersGroup(member.getCompany());

        apiSchemaGroup.addMemberByService(readers.getId(), member.getUserId());

        member.setStatus(CompanyMemberStatus.ACCEPTED);
    }

    private String getReadersGroupName(Company company){
        return String.format("%s_READERS_GROUP", company.getName().strip().toLowerCase());
    }

    private DbGroup getReadersGroup(Company company){
        return company.getReaderGroups().stream()
                .filter(g -> g.getName().equals(getReadersGroupName(company)))
                .findFirst()
                .orElse(null);
    }

    private String getManagersGroupName(Company company){
        return String.format("%s_MANAGERS_GROUP", company.getName().strip().toLowerCase());
    }

    private DbGroup getManagersGroup(Company company){
        List<DbGroup> temp = company.getEditorGroups().stream()
                .filter(g -> {
                    String gName = g.getName();
                    String mgName = getManagersGroupName(company);
                    return gName.equals(mgName);
                })
                .toList();

        return temp.getFirst();
    }
}
