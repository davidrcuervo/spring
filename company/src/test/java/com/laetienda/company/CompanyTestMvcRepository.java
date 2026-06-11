package com.laetienda.company;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.interfaces.InputOptions;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class CompanyTestMvcRepository {

    @Value("${api.company.create.uri}")
    protected String createCompanyUri;

    @Value("${api.company.isValid.uri}")
    protected String apiCompanyIsValidUri;

    @Value("${api.company.find.uri}")
    protected String apiCompanyFindUri;

    @Value("${api.company.findByName.uri}")
    private String findByNameUri;

    @Value("${api.company.find.vanityUrl.uri}")
    private String findByVanityUrlUri;

    @Value("${api.company.find.all.uri}")
    private String findAllUri;

    @Value("${api.company.update.uri.name}")
    protected String updateCompanyNameUri;

    @Value("${api.company.update.uri.content}")
    protected String apiCompanyUpdateContentUri;

    @Value("${api.company.delete.uri}")
    protected String deleteCompanyUri;

    @Value("${api.company.member.find.uri}")
    protected String apiCompanyMemberFindUri;

    @Value("${api.company.member.all.uri}")
    protected String getAllMembersUri;

    @Value("${api.company.member.add.uri}")
    protected String apiCompanyMemberAddUri;

    @Value("${api.company.member.update.uri}")
    protected String apiCompanyMemberUpdateUri;

    @Value("${api.company.member.delete.uri}")
    protected String apiCompanyMemberDeleteUri;

    @Value("${api.company.manager.uri.add}")
    protected String addManagerUri;

    @Value("${api.company.manager.uri.all}")
    private String getManagersUri;

    @Value("${api.company.manager.uri.remove}")
    protected String removeManagerUri;

    @Value("${api.company.policy.all.uri}")
    protected String apiCompanyPolicyAllUri;

    private final MockMvc mvc;
    private final ObjectMapper json;
    private final ToolBoxService tb;

    CompanyTestMvcRepository(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            ToolBoxService toolBoxService
    ){
        this.mvc = mockMvc;
        this.json = objectMapper;
        this.tb = toolBoxService;
    }

    Company create (
            String companyName,
            String vanityUrl,
            CompanyMemberPolicy companyMemberPolicy,
            TestUserDto user
    ) throws Exception{
        Company company = new Company(
                companyName,
                vanityUrl,
                companyMemberPolicy
        );
        company.setOwner(user.getUserId());

        MvcResult resp = mvc.perform(post(createCompanyUri)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(company))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getToken()))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    public Company findById(Long compId, String token) throws  Exception {
        MvcResult resp = mvc.perform(get(apiCompanyFindUri, compId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    public Company findByName(String name, String token) throws Exception{
        MvcResult resp = mvc.perform(get(findByNameUri, name)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    public Company findByVanityUrl(String vanityUrl, String token) throws Exception{
        MvcResult resp = mvc.perform(get(findByVanityUrlUri, vanityUrl)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    public List<Company> findAll(Map<String, String> params, String token) throws Exception{
        String address = tb.setAddressParams(params, findAllUri);
        MvcResult resp = mvc.perform(get(address)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        ).andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), new TypeReference<>(){});
    }

    public void deleteCompany(Long id, String token) throws Exception {
        mvc.perform(delete(deleteCompanyUri, id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    public Company updateName(Long id, String newCompanyName, String token) throws Exception {
        MvcResult resp = mvc.perform(put(updateCompanyNameUri, id, newCompanyName)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
                return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    public Company updateCompanyContent(Long companyId, Map<String, String> body, String token) throws Exception {
        MvcResult resp = mvc.perform(put(apiCompanyUpdateContentUri, companyId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(body)))
                .andExpect(status().isOk()).andReturn();
        return  json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    public List<Member> getAllMembers(long companyId, Map<String, String> params, String token) throws Exception {
        String address = tb.setAddressParams(params, getAllMembersUri, companyId);
        MvcResult resp = mvc.perform(get(address)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), new TypeReference<>(){});
    }

    public Member findMember(Long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(get(apiCompanyMemberFindUri, companyId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    public Member addMember(Long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(put(apiCompanyMemberAddUri, companyId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    public Member updateMember(Member member, String token) throws  Exception {
        MvcResult resp = mvc.perform(put(apiCompanyMemberUpdateUri)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(member)))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    public void deleteMember(Long companyId, String userId, String token) throws Exception {
        mvc.perform(delete(apiCompanyMemberDeleteUri, companyId, userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());

    }

    public Company addManager(long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(put(addManagerUri, companyId, userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        Company result = json.readValue(resp.getResponse().getContentAsString(), Company.class);
        assertNotNull(result);
        assertTrue(result.getEditorGroups().stream().anyMatch(g -> g.getMembers().contains(userId)));

        return result;
    }

    public Company removeManager(long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(delete(removeManagerUri, companyId, userId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        Company result = json.readValue(resp.getResponse().getContentAsString(), Company.class);
        assertNotNull(result);
        assertTrue(result.getEditorGroups().stream().noneMatch(g -> g.getMembers().contains(userId)));

        return result;
    }

    public List<InputOptions> getAllCompanyMemberPolicies(String token) throws Exception{
        MvcResult resp = mvc.perform(get(apiCompanyPolicyAllUri)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        List<CompanyMemberPolicy> temp = json.readValue(
                resp.getResponse().getContentAsString(),
                new TypeReference<List<CompanyMemberPolicy>>() {}
        );

        return temp.stream()
                .map(InputOptions.class::cast)
                .collect(Collectors.toList()
                );
    }

    public List<Member> getManagers(Long id, String token) throws Exception {
        MvcResult resp = mvc.perform(get(getManagersUri, id)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), new TypeReference<>(){});
    }
}
