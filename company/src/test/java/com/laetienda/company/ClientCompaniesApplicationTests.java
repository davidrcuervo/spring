package com.laetienda.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.utils.service.api.ApiUser;
import jdk.jfr.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ClientCompaniesApplicationTests {
    private final static Logger log = LoggerFactory.getLogger(ClientCompaniesApplicationTests.class);

	private static String jwtTestUser;
    private static String jwtAdminUser;

	@Autowired Environment env;
	@Autowired MockMvc mvc;
	@Autowired ObjectMapper json;
	@Autowired private ApiUser apiUser;

	@Value("${api.company.create.uri}")
	private String createAddress;

	@Value("${api.company.find.uri}")
	private String findAddress;

    @Value("${api.company.delete.uri}")
    private String deleteAddress;

	@Value("${webapp.user.test.userId}")
	private String testUserId;

    @Value("${webapp.user.admin.userId}")
    private String adminUserId;

    @Value("${api.company.member.find.uri}")
    private String findMemberAddress; //api/v0/company/member/find/{companyId}/{userId}

    @Value("${api.company.isValid.uri}")
    private String isCompanyValidUri; //api/v0/company/isValid/{companyId}

	@Test
	void health() throws Exception {
		String address = env.getProperty("api.actuator.health.path");
		assertNotNull(address);
		mvc.perform(get(address))
				.andExpect(status().isOk());
	}

	@Test
	void cycle() throws Exception{
        Company company = new Company("testCycleCompany", CompanyMemberPolicy.AUTHORIZATION_REQUIRED);
		company.setOwner(testUserId);

		Company comp = create(company);
		comp = findByName(comp.getName());
		comp = findById(comp.getId());
        Member member = addMember(comp);
        comp = updateCompany(comp);
        member = updateMember(member);
        //TODO: sendFriendRequest
        //TODO: acceptFriend
        //TODO: blockFriend
        //TODO: removeFriend
        //TODO: removeMember
        deleteCompany(comp);
	}

	private Company create(Company company) throws Exception {

		MvcResult response = mvc.perform(post(createAddress)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsBytes(company)))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

	private Company findById(Long id) throws Exception{
		MvcResult response = mvc.perform(get(findAddress, id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

	private Company findByName(String companyName) throws Exception {
		String address = env.getProperty("api.company.findByName.uri");
		assertNotNull(address);

		MvcResult response = mvc.perform(get(address, companyName)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

    private Company updateCompany(Company company) throws Exception {
        String address = env.getProperty("api.company.update.uri");
        String description = "Description of the company has been added.";
        Company temp = new Company("updateCompany", CompanyMemberPolicy.PUBLIC);
        temp.setOwner(adminUserId);
        assertNotNull(address);

        assertNull(company.getDescription());
        company.setDescription(description);

        MvcResult response = mvc.perform(post(createAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtAdminUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(temp)))
                .andExpect(status().isOk())
                .andReturn();
        temp = json.readValue(response.getResponse().getContentAsString(), Company.class);

        response = mvc.perform(put(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(company)))
                .andExpect(status().isOk())
                .andReturn();
        Company result = json.readValue(response.getResponse().getContentAsString(), Company.class);
        assertEquals(description, result.getDescription());

        company.setName(temp.getName());
        mvc.perform(put(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(company)))
                .andExpect(status().isForbidden());

        company.setName(result.getName());
        company.setMemberPolicy(CompanyMemberPolicy.PUBLIC);
        mvc.perform(put(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(company)))
                .andExpect(status().isForbidden());

        mvc.perform(delete(deleteAddress, temp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtAdminUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        return result;
    }

    private void deleteCompany(Company company) throws Exception {
        assertNotNull(deleteAddress);
        assertNotNull(company);

        Long id = company.getId();

        MvcResult response = mvc.perform(get(findAddress, id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(get(isCompanyValidUri, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(delete(deleteAddress, id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get(findAddress, id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        mvc.perform(delete(deleteAddress, id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(get(isCompanyValidUri, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Member addMember(Company company) throws Exception{
        String address = env.getProperty("api.company.member.add.uri");
        assertNotNull(address);

        String tmp = env.getProperty("webapp.user.admin.userId");
        String uid = apiUser.isUserIdValid(tmp);

        // check if member does not exist
        log.trace("COMPANY_TEST::addMember. $findMemberAddress: {}", findMemberAddress);
        mvc.perform(get(findMemberAddress, company.getId(), uid)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // add member request to company
        MvcResult resp = mvc.perform(put(address, company.getId(), uid)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getJwtAdminUser())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Member result = json.readValue(resp.getResponse().getContentAsString(), Member.class);
        assertEquals(CompanyMemberStatus.REQUESTED, result.getStatus());

        //check again if member exists
        resp = mvc.perform(get(findMemberAddress, company.getId(), uid)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        Member memb1 = json.readValue(resp.getResponse().getContentAsString(), Member.class);
        assertNotNull(memb1.getId());

        return result;
    }

    private Member updateMember(Member member) throws Exception{
        String address = env.getProperty("api.company.member.update.uri");
        assertNotNull(address);

        assertEquals(CompanyMemberStatus.REQUESTED, member.getStatus());
        member.setStatus(CompanyMemberStatus.ACCEPTED);

        MvcResult response = mvc.perform(put(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(member)))
                .andExpect(status().isOk()).andReturn();
        Member result = json.readValue(response.getResponse().getContentAsString(), Member.class);
        assertEquals(CompanyMemberStatus.ACCEPTED, result.getStatus());

        result.setUserId(testUserId);
        mvc.perform(put(address)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtTestUser)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(result)))
                .andExpect(status().isBadRequest());

        return result;
    }

	@Test
	public void createWithRepeatedName(){
		fail();
	}

    @Test
    public void findWrongCompany(){
        fail();
    }

    public String getJwtAdminUser() throws Exception{

        if(jwtAdminUser == null){
            String username = env.getProperty("webapp.user.admin.username");
            String password = env.getProperty("webapp.user.admin.password");
            jwtAdminUser = apiUser.getToken(username, password);
        }

        return jwtAdminUser;

    }

	@BeforeAll
	public static void setJwtTestUser(@Autowired ApiUser apiUSer, @Autowired Environment env) throws Exception{
		String testUsername = env.getProperty("webapp.user.test.username");
		assertNotNull(testUsername);

		String testUserPassword = env.getProperty("webapp.user.test.password");
		assertNotNull(testUserPassword);

		jwtTestUser = apiUSer.getToken(testUsername, testUserPassword);
	}
}
