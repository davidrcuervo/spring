package com.laetienda.company;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class CompanyTestMvcRepository {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Value("${api.company.create.uri}")
    protected String createCompanyUri;

    @Value("${api.company.isValid.uri}")
    protected String apiCompanyIsValidUri;

    @Value("${api.company.find.uri}")
    protected String apiCompanyFindUri;

    @Value("${api.company.update.uri.name}")
    protected String updateCompanyNameUri;

    @Value("${api.company.update.uri.content}")
    protected String apiCompanyUpdateContentUri;

    @Value("${api.company.delete.uri}")
    protected String deleteCompanyUri;

    @Value("${api.company.member.find.uri}")
    protected String apiCompanyMemberFindUri;

    @Value("${api.company.member.add.uri}")
    protected String apiCompanyMemberAddUri;

    @Value("${api.company.member.update.uri}")
    protected String apiCompanyMemberUpdateUri;

    @Value("${api.company.member.delete.uri}")
    protected String apiCompanyMemberDeleteUri;

    @Value("${api.company.policy.all.uri}")
    protected String apiCompanyPolicyAllUri;

    Company create (String companyName, CompanyMemberPolicy companyMemberPolicy, TestUserDto user) throws Exception{
        Company company = new Company(
                companyName,
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

    public Company findCompany(Long compId, String token) throws  Exception {
        MvcResult resp = mvc.perform(get(apiCompanyFindUri, compId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
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
}
