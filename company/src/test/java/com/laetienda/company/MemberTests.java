package com.laetienda.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(CompanyTestConfiguration.class)
public class MemberTests {

    private static TestUserDto[] USERS;

    @Autowired private ObjectMapper json;
    @Autowired private MockMvc mvc;

    @Value("${api.company.create.uri}")
    private String createCompanyAddress;

    @Value("${api.company.find.uri}")
    private String findCompanyByIdAddress;

    @Value("${api.company.delete.uri}")
    private String deleteAddress;

    @Value("${api.company.member.find.uri}")
    private String findMemberAddress;

    @Value("${api.company.member.add.uri}")
    private String addMemberAddress;

    @Value("${api.company.member.update.uri}")
    private String  updateMemberAddress;

    @Value("${api.company.manager.uri.add}")
    private String addCompanyManagerAddress;

    @Value("${api.company.member.delete.uri}")
    private String deleteMemberAddress;

    @Test
    public void findCompanyByNewMember() throws Exception {
        Company comp = getNewCompany(
                "Test Company findCompanyByNewMember",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member memb2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
        comp = findCompanyById(comp.getId(), USERS[2].getToken());

        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void addMemberByNoManger() throws Exception {
        Company comp = getNewCompany(
                "Test Company addMemberByNoManger",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member memb = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());

        mvc.perform(put(addMemberAddress, comp.getId(), USERS[3].getUserId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                        .andExpect(status().isUnauthorized());

        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void addManagerOfNotAcceptedMember() throws Exception {

        Company comp = getNewCompany("Test Company addManagerOfNotAcceptedMember",
                CompanyMemberPolicy.AUTHORIZATION_REQUIRED,
                USERS[1]);

        Member memb = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());

        mvc.perform(put(addCompanyManagerAddress, comp.getId(), memb.getUserId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isForbidden());

        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void removeMemberWhoIsOwnerOfCompany() throws Exception {
        Company comp = getNewCompany(
                "Test Company removeMemberWhoIsOwnerOfCompany",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]);

        mvc.perform(delete(deleteMemberAddress, comp.getId(), USERS[1].getUserId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                        .andExpect(status().isForbidden());

        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void testDeleteMember()throws Exception{
        Company comp = getNewCompany("Test Company deleteMember", CompanyMemberPolicy.PUBLIC, USERS[1]);
        Member member2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
        deleteMember(member2, USERS[2].getToken());
        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void updateMemberWithDifferentUserId() throws Exception {
        Company comp = getNewCompany(
                "Test Company updateMemberWithDifferentUserId",
                CompanyMemberPolicy.AUTHORIZATION_REQUIRED,
                USERS[1]
        );

        Member memb2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[1].getToken());
        memb2 = findMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());


        memb2.setStatus(CompanyMemberStatus.ACCEPTED);
        mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(memb2)))
                .andExpect(status().isUnauthorized());

        memb2 = updateMember(memb2, USERS[1].getToken());

        deleteCompany(comp, USERS[1].getToken());
    }

    private Company getNewCompany(String name, CompanyMemberPolicy policy, TestUserDto user) throws Exception{
        Company company = new Company(name, policy);
        company.setOwner(user.getUserId());

        MvcResult resp = mvc.perform(post(createCompanyAddress)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(company))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getToken()))
                        .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    private Company findCompanyById(Long companyId, String token) throws Exception{
        MvcResult resp = mvc.perform(get(findCompanyByIdAddress, companyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    private void deleteCompany(Company company, String token) throws Exception {
        mvc.perform(delete(deleteAddress, company.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                        .andExpect(status().isNoContent());
    }

    private Member findMember(Long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(get(findMemberAddress, companyId, userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    private Member addMember(Long companyId, String userId, String token) throws Exception{
        MvcResult resp = mvc.perform(put(addMemberAddress, companyId, userId)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                        .andExpect(status().isOk()).andReturn();

        return  json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    private Member updateMember(Member member, String token) throws Exception{
        MvcResult resp = mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(member)))
                .andExpect(status().isOk()).andReturn();
        return json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    private void deleteMember(Member member, String token) throws Exception {
        mvc.perform(delete(deleteMemberAddress, member.getCompany().getId(), member.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @BeforeAll
    public static void setUp(@Autowired UtilsBox utils){
        USERS = utils.getTestUsers(3, "CompanyMemberTest");
    }

    @AfterAll
    public static void tearDown(@Autowired UtilsBox utils){
        utils.deleteTestUsers(USERS);
    }
}
