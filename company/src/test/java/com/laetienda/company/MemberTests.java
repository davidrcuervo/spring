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

import java.util.Map;

import static org.assertj.core.api.Fail.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(CompanyTestConfiguration.class)
public class MemberTests {

    private static TestUserDto[] USERS;

    @Autowired private ObjectMapper json;
    @Autowired private MockMvc mvc;
    @Autowired private CompanyTestMvcRepository repo;

    @Value("${api.company.create.uri}")
    private String createCompanyAddress;

    @Value("${api.company.find.uri}")
    private String apiCompanyFindUri;

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
    public void cycle() throws Exception {
        Company company = repo.create(
                "Test Company - Company Member Cycle",
                "tc-cmc",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member member1 = repo.findMember(company.getId(), USERS[1].getUserId(), USERS[1].getToken());
        Member member2 = repo.addMember(company.getId(), USERS[2].getUserId(), USERS[1].getToken());

        company = repo.updateCompanyContent(company.getId(), Map.of("owner", member2.getId().toString()), USERS[1].getToken());

        member1.setStatus(CompanyMemberStatus.BLOCKED);
        member1 = repo.updateMember(member1, USERS[2].getToken());

        repo.deleteMember(company.getId(), USERS[1].getUserId(), USERS[1].getToken());
        mvc.perform(get(findMemberAddress, company.getId(), USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        repo.deleteCompany(company.getId(), USERS[2].getToken());

        mvc.perform(get(apiCompanyFindUri, company.getId())
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                        .andExpect(status().isNotFound());
    }

    @Test
    public void findCompanyByNewMember() throws Exception {
        Company comp = getNewCompany(
                "Test Company - Find Company By New Member",
                "tc-fcbnm",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member memb2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
        comp = findCompanyById(comp.getId(), USERS[2].getToken());

        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void blockMember() throws Exception {
        Company comp = repo.create(
                "Test Company Block Member",
                "tc-cmc",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member member2 = repo.addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
//        Member member1 = repo.findMember(comp.getId(), USERS[1].getUserId(), USERS[2].getToken());
        comp = repo.findById(comp.getId(), USERS[2].getToken());

        member2.setStatus(CompanyMemberStatus.BLOCKED);
        member2 = repo.updateMember(member2, USERS[2].getToken());

        mvc.perform(get(repo.apiCompanyFindUri, comp.getId())
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                        .andExpect(status().isUnauthorized());

//        mvc.perform(get(repo.apiCompanyMemberFindUri, comp.getId(), USERS[1].getUserId())
//                        .accept(MediaType.APPLICATION_JSON)
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
//                .andExpect(status().isUnauthorized());

        repo.deleteCompany(comp.getId(), USERS[1].getToken());
    }

    @Test
    public void updateMemberUserId() throws Exception {
        Company company = repo.create(
                "Test Company - Update Member User Id",
                "tc-umui",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member member1 = repo.findMember(company.getId(), USERS[1].getUserId(), USERS[1].getToken());
        member1.setUserId(USERS[2].getUserId());

        mvc.perform(put(updateMemberAddress, company.getId(), USERS[1].getUserId(), USERS[1].getToken())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(member1)))
                .andExpect(status().isBadRequest());

        repo.deleteCompany(company.getId(), USERS[1].getToken());
    }

    @Test
    public void addMemberByNoManger() throws Exception {
        Company comp = getNewCompany(
                "Test Company - Add Member By No Manger",
                "tc-ambnm",
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

        Company comp = getNewCompany(
                "Test Company - Add Manager Of Not Accepted Member",
                "tc-amonam",
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
                "Test Company - Remove Member Who Is Owner Of Company",
                "tc-rmwiooc",
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
        Company comp = getNewCompany(
                "Test Company - Delete Member",
                "tc-dm",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]);
        Member member2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
        deleteMember(member2, USERS[2].getToken());
        deleteCompany(comp, USERS[1].getToken());
    }

    @Test
    public void updateMemberWithDifferentUserId() throws Exception {
        Company comp = getNewCompany(
                "Test Company - Update Member With Different User Id",
                "tc-umwdui",
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

    private Company getNewCompany(
            String name,
            String vanityUrl,
            CompanyMemberPolicy policy,
            TestUserDto user
    ) throws Exception{
        Company company = new Company(name, vanityUrl, policy);
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
        MvcResult resp = mvc.perform(get(apiCompanyFindUri, companyId)
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
