package com.laetienda.company;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.options.CompanyFriendStatus;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Friend;
import com.laetienda.model.company.Member;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.utils.service.api.ApiUser;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(CompanyTestConfiguration.class)
class CompanyTests {
    private final static Logger log = LoggerFactory.getLogger(CompanyTests.class);

    private static TestUserDto[] USERS;

	@Autowired Environment env;
	@Autowired MockMvc mvc;
	@Autowired ObjectMapper json;
	@Autowired private ApiUser apiUser;
    @Autowired private CompanyTestMvcRepository repo;

	@Value("${api.company.create.uri}")
	private String createAddress;

	@Value("${api.company.find.uri}")
	private String findAddress;

    @Value("${api.company.delete.uri}")
    private String deleteAddress;

    @Value("${api.company.update.uri.content}")
    private String companyUpdateContentAddress;

    @Value("${api.company.update.uri.description}")
    private String companyUpdateDescriptionAddress;

    @Value("${api.company.member.find.uri}")
    private String findMemberAddress; //api/v0/company/member/find/{companyId}/{userId}

    @Value("${api.company.isValid.uri}")
    private String isCompanyValidUri; //api/v0/company/isValid/{companyId}

    @Value("${api.company.member.add.uri}")
    private String addMemberAddress;

    @Value("${api.company.member.update.uri}")
    private String updateMemberAddress;

    @Value("${api.company.member.delete.uri}")
    private String deleteMemberAddress; //api/v0/company/member/delete/{memberId}

    @Value("${api.company.friend.uri.find}")
    private String findFriendAddress;

    @Value("${api.company.friend.uri.add}")
    private String addFriendAddress;

    @Value("${api.company.manager.uri.add}")
    private String addManagerAddress;

	@Test
	void health() throws Exception {
		String address = env.getProperty("api.actuator.health.path");
		assertNotNull(address);
		mvc.perform(get(address))
				.andExpect(status().isOk());
	}

	@Test
	void cycle() throws Exception{

        Company company = repo.create(
                "Test Cycle Company",
                "testCycleCompany",
                CompanyMemberPolicy.AUTHORIZATION_REQUIRED,
                USERS[1]
        );

		company = repo.findByName(company.getName(), USERS[1].getToken());
        company = repo.findByVanityUrl(company.getVanityUrl(), USERS[1].getToken());
		company = repo.findById(company.getId(), USERS[1].getToken());
        Member member = addMember(company);
        company = updateCompany(company);
        member = updateMember(member);
        Friend friend = sendFriendRequest(member);
        friend = acceptFriend(friend);
        friend = blockFriend(friend);
        friend = unblockFriend(friend);
        company = companyAddManager(company);
        company = modifyCompanyOwner(company);
        company = deleteOldOwnerMember(company);
//        deleteMember(member);
        deleteCompany(company);
	}

    private Company create(Company company) throws Exception {

		MvcResult response = mvc.perform(post(createAddress)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(json.writeValueAsBytes(company)))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

	private Company findById(Long id) throws Exception{
		MvcResult response = mvc.perform(get(findAddress, id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

	private Company findByName(String companyName) throws Exception {
		String address = env.getProperty("api.company.findByName.uri");
		assertNotNull(address);

		MvcResult response = mvc.perform(get(address, companyName)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();

		return json.readValue(response.getResponse().getContentAsString(), Company.class);
	}

    private Company updateCompany(Company company) throws Exception {

        //UPDATE DESCRIPTION
        String description = "Description of the company has been added.";
        String address = env.getProperty("api.company.update.uri.description");
        assertNotNull(address);
        assertNull(company.getDescription());

        MvcResult response = mvc.perform(put(address, company.getId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(description))
                .andExpect(status().isOk())
                .andReturn();

        Company result = json.readValue(response.getResponse().getContentAsString(), Company.class);
        assertEquals(description, result.getDescription());

        //CREATE COMPANY FOR TESTING UPDATES
        Company temp = new Company(
                "Update Company",
                "updateCompany",
                CompanyMemberPolicy.PUBLIC
        );
        temp.setOwner(USERS[2].getUserId());

        response = mvc.perform(post(createAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(temp)))
                .andExpect(status().isOk())
                .andReturn();
        temp = json.readValue(response.getResponse().getContentAsString(), Company.class);

        //UPDATE COMPANY NAME
//      company.setName(temp.getName());
        address = env.getProperty("api.company.update.uri.name");
        assertNotNull(address);

        mvc.perform(put(address, company.getId(), temp.getName())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        company.setName(result.getName());

        //DELETE TEMP COMPANY
        mvc.perform(delete(deleteAddress, temp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
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
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        mvc.perform(get(isCompanyValidUri, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(delete(deleteAddress, id)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        mvc.perform(get(findAddress, id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        mvc.perform(delete(deleteAddress, id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        mvc.perform(get(isCompanyValidUri, id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Member addMember(Company company) throws Exception{

        // check if member does not exist
        log.trace("COMPANY_TEST::addMember. $findMemberAddress: {}", findMemberAddress);
        mvc.perform(get(findMemberAddress, company.getId(), USERS[2].getUserId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        //add member that already exists
        mvc.perform(put(addMemberAddress, company.getId(), USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isForbidden());

        //add member request to company
        Member result = repo.addMember(company.getId(), USERS[2].getUserId(), USERS[2].getToken());
        assertEquals(CompanyMemberStatus.REQUESTED, result.getStatus());

        //check again if member exists
        Member memb1 = repo.findMember(company.getId(), USERS[2].getUserId(), USERS[1].getToken());
        assertNotNull(memb1.getId());

        return result;
    }

    private Member updateMember(Member member) throws Exception{

        assertEquals(CompanyMemberStatus.REQUESTED, member.getStatus());
        member.setStatus(CompanyMemberStatus.ACCEPTED);

        mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(member)))
                .andExpect(status().isUnauthorized());

        MvcResult response = mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(member)))
                .andExpect(status().isOk()).andReturn();
        Member result = json.readValue(response.getResponse().getContentAsString(), Member.class);
        assertEquals(CompanyMemberStatus.ACCEPTED, result.getStatus());

        member.setUserId(USERS[1].getUserId());
        mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(member)))
                .andExpect(status().isBadRequest());

        return result;
    }

//    private void deleteMember(Member member) throws Exception {
//        mvc.perform(get(findMemberAddress, member.getCompany().getId(), member.getUserId())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk());
//
//        mvc.perform(delete(deleteMemberAddress, member.getCompany().getId(), member.getUserId())
//                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
//                .andExpect(status().isNoContent());
//
//        mvc.perform(get(findMemberAddress, member.getCompany().getId(), member.getUserId())
//                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }

    private Friend sendFriendRequest(Member member)throws Exception{

        Long cid = member.getCompany().getId();

        //friend/find/{companyId}/{memberUserId}/{friendUserId}
        mvc.perform(get(findFriendAddress, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isNotFound());

        //friend/add/{companyId}/{memberUserId}/{friendUserId}
        MvcResult response = mvc.perform(put(addFriendAddress, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Friend result = json.readValue(response.getResponse().getContentAsString(), Friend.class);
        assertEquals(CompanyFriendStatus.REQUESTED, result.getStatus());

        mvc.perform(get(findFriendAddress, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(put(addFriendAddress, cid, USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isForbidden());

        mvc.perform(put(addFriendAddress, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isForbidden());

        mvc.perform(get(findFriendAddress, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isForbidden());

        return result;
    }

    private Friend acceptFriend(Friend friend) throws Exception {
        String address = env.getProperty("api.company.friend.uri.accept"); //api/v0/company/friend/accept/{companyId}/{memberUserId}/{buddyUserId}
        assertNotNull(address);

        Long cid = friend.getMember().getCompany().getId();

        MvcResult response = mvc.perform(put(address, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isOk())
                .andReturn();
        Friend result = json.readValue(response.getResponse().getContentAsString(), Friend.class);
        assertEquals(CompanyFriendStatus.ACCEPTED, result.getStatus());

        mvc.perform(put(address, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isForbidden());

        mvc.perform(put(address, cid, "not-valid-user-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isNotFound());

        return result;
    }

    private Friend blockFriend(Friend friend) throws Exception {
        String addressBlock = env.getProperty("api.company.friend.uri.block");
        assertNotNull(addressBlock);

        Long cid = friend.getMember().getCompany().getId();

        MvcResult response = mvc.perform(put(addressBlock, cid, USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Friend result = json.readValue(response.getResponse().getContentAsString(), Friend.class);
        assertEquals(CompanyFriendStatus.BLOCKED_BY_RECEIVER, result.getStatus());

        mvc.perform(put(addressBlock, cid, "not-valid-user-id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isNotFound());

        return result;
    }

    private Friend unblockFriend(Friend friend) throws Exception {
        String address = env.getProperty("api.company.friend.uri.unblock");
        assertNotNull(address);

        Long cid = friend.getMember().getCompany().getId();

        mvc.perform(put(address, cid, USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isForbidden());

        MvcResult response = mvc.perform(put(address, cid, USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        Friend result = json.readValue(response.getResponse().getContentAsString(), Friend.class);
        assertEquals(CompanyFriendStatus.ACCEPTED, result.getStatus());

        mvc.perform(put(address, cid, USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isBadRequest());

        return result;
    }

    private Company companyAddManager(Company comp) throws Exception {
        final String companyName = comp.getName();
        assertFalse(comp.getEditors().contains(USERS[2].getUserId()));

        //BAD_REQUEST: Add manager that is same owner
        mvc.perform(put(addManagerAddress, comp.getId(), USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isOk());

        //UNAUTHORIZED: Add manager by user that is not manager
        mvc.perform(put(addManagerAddress, comp.getId(), USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken()))
                .andExpect(status().isUnauthorized());

        //SUCCESSFUL: Add manager by owner.
        MvcResult response = mvc.perform(put(addManagerAddress, comp.getId(), USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        Company result = json.readValue(response.getResponse().getContentAsString(), Company.class);
        DbGroup managers = result.getEditorGroups().stream()
                .filter(g -> g.getName().equals(companyName.strip().toLowerCase() + "_MANAGERS_GROUP"))
                .findFirst().orElse(null);
        assertNotNull(managers);
        assertTrue(managers.getMembers().contains(USERS[2].getUserId()));

        //OK: Add manager that is already manager
        mvc.perform(put(addManagerAddress, comp.getId(), USERS[2].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isOk());

        //FORBIDDEN: Block company owner by new manager
        response = mvc.perform(get(findMemberAddress, comp.getId(), USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();
        Member member = json.readValue(response.getResponse().getContentAsString(), Member.class);

        member.setStatus(CompanyMemberStatus.BLOCKED);
        mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(member)))
                .andExpect(status().isForbidden());

        //OK: Add another manager by 2nd manager (requires extra user).
        Member memb3 = addMember(comp.getId(), USERS[3].getUserId(), USERS[3].getToken());
        memb3.setStatus(CompanyMemberStatus.ACCEPTED);
        memb3 = updateMember(memb3, USERS[1].getToken());
        comp = addManager(memb3, USERS[2].getToken());

        //OK: Block member who is 2nd manager by owner
        Member memb2 = findMember(comp.getId(), USERS[2].getUserId(), USERS[3].getToken());
        memb2.setStatus(CompanyMemberStatus.BLOCKED);
        memb2 = updateMember(memb2, USERS[1].getToken());

        //UNAUTHORIZED: Find company by blocked member
        mvc.perform(get(findAddress, comp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        //OK: Unblock member who was 2nd manager by 3rd manager
        memb2.setStatus(CompanyMemberStatus.ACCEPTED);
        memb2 = updateMember(memb2, USERS[3].getToken());

        return findCompanyById(comp.getId(), USERS[2].getToken());
    }

    private Company modifyCompanyOwner(Company comp) throws Exception {

        Member member2 = findMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
        Map<String, String> body = Map.of("owner", member2.getId().toString());

        Company result = updateCompanyContent(comp.getId(), body, USERS[1].getToken());

        return findCompanyById(comp.getId(), USERS[2].getToken());
    }

    private Company deleteOldOwnerMember(Company comp) throws Exception {
        Member member1 = findMember(comp.getId(), USERS[1].getUserId(), USERS[1].getToken());
        deleteMember(member1, USERS[1].getToken());

        mvc.perform(get(findMemberAddress, comp.getId(), USERS[1].getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        return findCompanyById(comp.getId(), USERS[2].getToken());
    }

    @Test
    public void findCompanyWithInvalidId() throws Exception {
        mvc.perform(get(findAddress, 1)
                                .accept(MediaType.APPLICATION_JSON)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                        .andExpect(status().isNotFound());
    }

    @Test
    public void createCompanyWithWrongOwner() throws Exception {
        Company company = new Company(
                "Test Company Create Company With Wrong Owner",
                "tcccwwo",
                CompanyMemberPolicy.PUBLIC
        );
        company.setOwner(USERS[2].getUserId());

        mvc.perform(post(createAddress)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json.writeValueAsString(company))
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                        .andExpect(status().isBadRequest());
    }

	@Test
	public void createCompanyWithRepeatedName() throws Exception {
		Company comp = getNewCompany(
                "Test Company Create Company With Repeated Name",
                "tc-ccwrn",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Company company = new Company(
                "Test Company Create Company With Repeated Name",
                "tcccwrn",
                CompanyMemberPolicy.PUBLIC);
        company.setOwner(USERS[1].getUserId());

        mvc.perform(post(createAddress)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(company))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken()))
                .andExpect(status().isForbidden());

        deleteCompany(comp.getId(), USERS[1].getToken());
	}

    @Test
    public void findWrongCompany() throws Exception {
        mvc.perform(get(findAddress, "bad id")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        mvc.perform(get(findAddress, 1023)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test void updateCompanyName() throws Exception {

        Company company = repo.create(
                "Test Company Update Company Name",
                "tcucn",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        company = repo.updateName(company.getId(), "Test Company newUpdateCompanyName", USERS[1].getToken());
        repo.deleteCompany(company.getId(), USERS[1].getToken());
    }

    @Test
    public void updateCompanyOwner() throws Exception {
        Company comp = getNewCompany(
                "Test Company Update Company Owner",
                "tc-uco",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member memb2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());

        Map<String, String> body = Map.of("owner", memb2.getId().toString());
        updateCompanyContent(comp.getId(), body, USERS[1].getToken());

        deleteCompany(comp.getId(), USERS[2].getToken());
    }

    @Test
    public void updateCompanyByBlockedMember() throws Exception{
        Company comp = getNewCompany(
                "Test Company Update Company By Blocked Member",
                "tc-ucbbm",
                CompanyMemberPolicy.AUTHORIZATION_REQUIRED,
                USERS[1]
        );

        Member memb2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[1].getToken());

        mvc.perform(put(companyUpdateDescriptionAddress, comp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[2].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("new description of the company"))
                .andExpect(status().isUnauthorized());

        deleteCompany(comp.getId(), USERS[1].getToken());
    }

    @Test
    public void updateCompanyContentBadKey() throws Exception{
        Company comp = getNewCompany(
                "Test Company Update Company Content Bad Key",
                "tc-uccbk",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Map<String, String> body = Map.of("bad Key", "Anything");
        mvc.perform(put(companyUpdateContentAddress, comp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest()).andReturn();

        deleteCompany(comp.getId(), USERS[1].getToken());
    }

    @Test
    public void updateCompanyOwnerByBlockedMember() throws Exception{
        Company comp = getNewCompany(
                "Test Company Update Company Owner By Blocked Member",
                "tc-ucobbm",
                CompanyMemberPolicy.AUTHORIZATION_REQUIRED,
                USERS[1]
        );

        Member memb2 = addMember(comp.getId(), USERS[2].getUserId(), USERS[2].getToken());
        Member memb3 = addMember(comp.getId(), USERS[3].getUserId(), USERS[3].getToken());

        Map<String, String> body = Map.of("owner", memb2.getId().toString());
        mvc.perform(put(companyUpdateContentAddress, comp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        //BAD REQUEST: also try by using a non-existent member id
        long memberId = 13445L;
        body = Map.of("owner", Long.toString(memberId));
        mvc.perform(put(companyUpdateContentAddress, comp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + USERS[1].getToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        memb2.setStatus(CompanyMemberStatus.ACCEPTED);
        memb2 =  updateMember(memb2, USERS[1].getToken());

        body = Map.of("owner", memb2.getId().toString());
        comp = updateCompanyContent(comp.getId(), body, USERS[1].getToken());

        deleteCompany(comp.getId(), USERS[2].getToken());
    }

    @Test
    public void getAllCompanyMemberPolicies() throws Exception {
        InputOptions pub = CompanyMemberPolicy.PUBLIC;
        InputOptions reg = CompanyMemberPolicy.REGISTRATION_REQUIRED;
        InputOptions auth = CompanyMemberPolicy.AUTHORIZATION_REQUIRED;

        List<InputOptions> result = repo.getAllCompanyMemberPolicies(USERS[0].getToken());
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(policy -> pub.getLabel().equals(policy.getLabel())));
        assertTrue(result.stream().anyMatch(policy -> reg.getLabel().equals(policy.getLabel())));
        assertTrue(result.stream().anyMatch(policy -> auth.getLabel().equals(policy.getLabel())));
    }

    @Test
    public void findAll() throws Exception {
        Company comp1 = repo.create("Test Company - Find All - 1", "tc-fa1", CompanyMemberPolicy.PUBLIC, USERS[1]);
        Company comp2 = repo.create("Test Company - Find All - 2", "tc-fa2", CompanyMemberPolicy.PUBLIC, USERS[2]);
        Company comp3 = repo.create("Test Company - Find All - 3", "tc-fa3", CompanyMemberPolicy.PUBLIC, USERS[3]);

        Member memb21 = repo.addMember(comp2.getId(), USERS[1].getUserId(), USERS[1].getToken());
        Member memb31 = repo.addMember(comp3.getId(), USERS[1].getUserId(), USERS[1].getToken());

        repo.addManager(comp2.getId(), memb21.getUserId(), USERS[2].getToken());

        List<Company> all = repo.findAll(Map.of("manager", ""), USERS[1].getToken());
        assertNotNull(all);
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(company -> comp1.getId().equals(company.getId())));
        assertTrue(all.stream().anyMatch(company -> comp2.getId().equals(company.getId())));
        assertTrue(all.stream().noneMatch(company -> comp3.getId().equals(company.getId())));

        repo.deleteCompany(comp1.getId(), USERS[1].getToken());
        repo.deleteCompany(comp2.getId(), USERS[2].getToken());
        repo.deleteCompany(comp3.getId(), USERS[3].getToken());
    }

    @Test
    public void findManagers() throws Exception {
        Company company = repo.create(
                "Test Company - Find Manager",
                "tc-fm",
                CompanyMemberPolicy.PUBLIC,
                USERS[1]
        );

        Member member2 = repo.addMember(company.getId(), USERS[2].getUserId(), USERS[2].getToken());
        repo.addMember(company.getId(), USERS[3].getUserId(), USERS[3].getToken());

        repo.addManager(company.getId(), member2.getUserId(), USERS[1].getToken());

        List<Member> managers = repo.getManagers(company.getId(), USERS[2].getToken());
        assertNotNull(managers);
        assertEquals(2, managers.size());
        assertTrue(managers.stream().anyMatch(
                member -> member.getUserId().equals(USERS[2].getUserId()))
        );
        assertTrue(managers.stream().anyMatch(
                member -> member.getUserId().equals(USERS[1].getUserId()))
        );
        assertTrue(managers.stream().noneMatch(
                member -> member.getUserId().equals(USERS[3].getUserId()))
        );

        repo.deleteCompany(company.getId(), USERS[1].getToken());
    }

    private Company getNewCompany(
            String name,
            String vanityUrl,
            CompanyMemberPolicy policy,
            TestUserDto user
    ) throws Exception{
        Company company = new Company(name, vanityUrl, policy);
        company.setOwner(user.getUserId());

        MvcResult resp = mvc.perform(post(createAddress)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(company))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.getToken()))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    private Company findCompanyById(Long companyId, String token) throws Exception{
        MvcResult result = mvc.perform(get(findAddress, companyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(result.getResponse().getContentAsString(), Company.class);
    }

    private Company updateCompanyContent(Long companyId, Map<String, String> body, String token) throws Exception{
        MvcResult resp = mvc.perform(put(companyUpdateContentAddress, companyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(body)))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), Company.class);
    }

    private Member addMember(Long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(put(addMemberAddress, companyId, userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        return  json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    private Member findMember(Long companyId, String userId, String token) throws Exception {
        MvcResult resp = mvc.perform(get(findMemberAddress, companyId, userId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        return json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    private Member updateMember(Member memb, String token) throws Exception {
        MvcResult resp = mvc.perform(put(updateMemberAddress)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsBytes(memb)))
                .andExpect(status().isOk()).andReturn();

        return  json.readValue(resp.getResponse().getContentAsString(), Member.class);
    }

    private void deleteMember(Member memb, String token) throws Exception {
        mvc.perform(delete(deleteMemberAddress, memb.getCompany().getId(), memb.getUserId(), token)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private Company addManager(Member member, String token) throws Exception {
        MvcResult resp = mvc.perform(put(addManagerAddress, member.getCompany().getId(), member.getUserId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn();

        Company result = json.readValue(resp.getResponse().getContentAsString(), Company.class);

        DbGroup managers = result.getEditorGroups().stream()
                .filter(g -> g.getName()
                        .equals(result.getName().strip().toLowerCase() + "_MANAGERS_GROUP"))
                .findFirst().orElse(null);
        assertNotNull(managers);
        assertTrue(managers.getMembers().contains(member.getUserId()));

        return result;
    }

    private void deleteCompany(Long companyId, String token) throws Exception {
        mvc.perform(delete(deleteAddress, companyId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNoContent());
    }


    @BeforeAll
    static public void setUp(@Autowired UtilsBox utils){
        USERS = utils.getTestUsers(3, "company.CompanyTest");
    }

    @AfterAll
    static public void tearDown(@Autowired UtilsBox utils){
        utils.deleteTestUsers(USERS);
    }
}
