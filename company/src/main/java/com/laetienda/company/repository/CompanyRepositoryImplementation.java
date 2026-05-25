package com.laetienda.company.repository;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Repository
public class CompanyRepositoryImplementation implements CompanyRepository{
    private final static Logger log = LoggerFactory.getLogger(CompanyRepositoryImplementation.class);

    @Value("${kc.client-registration-id.webapp}")
    private String webappClientId;

    private final RestClient client;
    private final ApiSchemaGroup apiSchemaGroup;
    private final ApiSchema apiSchema;
    private final Environment env;

    public CompanyRepositoryImplementation(
            RestClient restClient,
            ApiSchemaGroup apiSchemaGroup,
            ApiSchema apiSchema,
            Environment environment
    ){
        this.client= restClient;
        this.apiSchemaGroup = apiSchemaGroup;
        this.apiSchema = apiSchema;
        this.env = environment;
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

        return apiSchema.create(Company.class, company);
    }

    @Override
    public Long isCompanyValid(Long id) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::isCompanyValid. $companyId: {}", id);
        return apiSchema.isItemValid(Company.class, id);
    }

    @Override
    public Company findByName(String name) throws HttpStatusCodeException {
        return apiSchema.find(Company.class, Map.of("name", name));
    }

    @Override
    public Company findByNameNoJwt(String name) throws HttpStatusCodeException {
        return apiSchema.findByServiceId(Company.class, Map.of("name", name));
    }

    @Override
    public Company find(Long id) throws HttpStatusCodeException {

        return apiSchema.findById(Company.class, id);
    }

    @Override
    public Company findNoJwt(Long id) throws HttpStatusCodeException {
        log.debug("COMPANY_REPO::findNoJwt. $id: {}", id);

        String address = env.getProperty("api.schema.findById.uri", "findById");
        String clazzName = apiSchema.getClazzName(Company.class);
        return client.get().uri(address, id.toString(), clazzName)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(clientRegistrationId(webappClientId))
                .retrieve().toEntity(Company.class).getBody();
    }

    @Override
    public Company findByVanityUrl(String vanityUrl) throws HttpStatusCodeException {
        log.debug("REPO_COMPANY::findByVanityUrl | $vanityUrl: {}", vanityUrl);
        return apiSchema.find(Company.class, Map.of("vanityUrl", vanityUrl));
    }

    @Override
    public List<Company> findAll(Map<String, String> params) throws HttpStatusCodeException {
        log.debug("REPO_COMPANY::findAll");

        Map<String, String> converted =  new HashMap<>();

        if(params != null && !params.isEmpty()){
            params.forEach((key, value) -> {
                if(key.equals("manager")){
                    converted.put("editor", value);
                }else if(key.equals("member")){
                    converted.put("reader", value);
                }
            });
        }

        return apiSchema.findAll(Company.class, converted);
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

        apiSchema.deleteById(Company.class, company.getId());
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
        return apiSchema.findById(Member.class, memberId);
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
        return apiSchema.findByQuery(Member.class, Map.of("query", query));
    }

    private List<Member> findMembersByQueryNoJwt(String query) throws HttpStatusCodeException{
        return apiSchema.findByQueryByClientRegistrationId(Member.class, Map.of("query", query));
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

        apiSchema.deleteById(Member.class, member.getId());
    }

    @Override
    public Member updateMember(Member member) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::updateMember. $memberId: {}", member.getId());
        return apiSchema.update(Member.class, member);
    }

    @Override
    public Company updateCompany(Company company) throws HttpStatusCodeException {
        log.debug("COMPANY_REPOSITORY::updateCompany. $company: {}", company.getName());
        return apiSchema.update(Company.class, company);

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
        return apiSchema.create(Member.class, member);
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
