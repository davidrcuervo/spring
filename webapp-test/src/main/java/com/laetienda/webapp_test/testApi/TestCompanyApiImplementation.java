package com.laetienda.webapp_test.testApi;

import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.service.api.ApiCompany;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class TestCompanyApiImplementation implements TestCompanyApi {
    private final static Logger log = LoggerFactory.getLogger(TestCompanyApiImplementation.class);
    private final ApiCompany apiCompany;

    public TestCompanyApiImplementation(ApiCompany apiCompany) {
        this.apiCompany = apiCompany;
    }

    @Override
    public Company create(
            String name,
            String vanityUrl,
            CompanyMemberPolicy companyMemberPolicy,
            TestUserDto user
    ) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::create | Testing create");

        Company company = new Company(name,  vanityUrl, companyMemberPolicy);
        company.setOwner(user.getUserId());

        company = apiCompany.createCompany(company, user.getToken());
        assertNotNull(company, "TEST_COMPANY::cycle. Company could not be created");

        return company;
    }

    @Override
    public String isValid(Long id, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::isValid | Testing if company is valid");

        String result = apiCompany.isValid(id, token);
        assertEquals(id, Long.parseLong(result), "TEST_COMPANY::isValid | result is not company id in string format");

        return result;
    }

    @Override
    public Company find(Long id, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::find | Testing company api find");

        Company result = apiCompany.find(id, token);
        assertNotNull(result, "TEST_COMPANY::find | Company could not be found");
        return result;
    }

    @Override
    public List<Company> findAll(Map<String, String> params, String token) throws HttpStatusCodeException, AssertionError{
        log.debug("TEST_COMPANY::findAll | ");
        List<Company> result = apiCompany.findAllWithToken(params, token);
        assertNotNull(result);

        return result;
    }

    @Override
    public Company findByName(String name, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::findByName | Testing company api find by name");

        Company result = apiCompany.findByName(name, token);
        assertNotNull(result, "TEST_COMPANY::findByName | Company could not be found");
        return result;
    }

    @Override
    public Company findByVanityUrl(String vanityUrl, String token) throws HttpStatusCodeException {
        log.debug("TEST_COMPANY::findByName | $vanityUrl: {}", vanityUrl);
        Company result = apiCompany.findByVanityUrlWithToken(vanityUrl, token);
        assertNotNull(result, "TEST_COMPANY::findByName | Company could not be found");
        return result;
    }

    @Override
    public Company updateName(Long id, String newName, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::updateName | Testing company api update name");

        Company result = apiCompany.updateName(id, newName, token);
        assertNotNull(result, "TEST_COMPANY::updateName | Failed to update company name");
        assertEquals(newName, result.getName(), "TEST_COMPANY::updateName | Failed to update company name");

        return result;
    }

    @Override
    public Company updateDescription(Long id, String description, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::updateDescription | Testing company api update description");

        Company result = apiCompany.updateDescription(id, description, token);
        assertNotNull(result, "TEST_COMPANY::updateDescription | Company could not be found");
        assertEquals(description, result.getDescription(), "TEST_COMPANY::updateDescription | Description didn't update correctly");

        return result;
    }

    @Override
    public Company updateContent(Long id, Map<String, String> body, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::updateContent | Testing company api update content");

        Company result = apiCompany.updateContent(id, body, token);
        assertNotNull(result, "TEST_COMPANY::findByName | Company could not be found");
        return result;
    }

    @Override
    public void delete(Long id, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::delete | Testing delete");

        apiCompany.deleteCompany(id, token);

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> apiCompany.find(id, token),
                "TEST_COMPANY::cycle. Company could not be deleted"
        );
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode(), "TEST_COMPANY::cycle. Find company didn't return 404");
    }

    @Override
    public Member addMember(Long companyId, String userId, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::addMember | Testing add member");

        Member result = apiCompany.addMember(companyId, userId, token);
        assertNotNull(result, "TEST_COMPANY::addMember | Failed to add member");

        return result;
    }

    @Override
    public Member findMember(Long companyId, String userId, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::findMember | Testing find member");

        Member result = apiCompany.findMember(companyId, userId, token);
        assertNotNull(result, "TEST_COMPANY::findMember | Failed to find member");

        return result;
    }

    @Override
    public Member updateMember(Member member, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::updateMember | Testing update member");

        Member result = apiCompany.updateMember(member, token);
        assertNotNull(result, "TEST_COMPANY::updateMember | Failed to update member");

        return result;
    }

    @Override
    public void deleteMember(Long companyId, String userId, String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::deleteMember | Testing delete company member");

        apiCompany.deleteMember(companyId, userId, token);

        HttpStatusCodeException e = assertThrows(HttpStatusCodeException.class,
                () -> {apiCompany.findMember(companyId, userId, token);},
                "TEST_COMPANY::deleteMember | Failed to delete member"
        );
        assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode(), "TEST_COMPANY::deleteMember | Different status code to NOT FOUND");
    }

    @Override
    public Company addManager(long companyId, String userId, String token) throws HttpStatusCodeException, AssertionError{
        log.debug("TEST_COMPANY::addManager | Testing add manager");

        Company result = apiCompany.addManagerWithToken(companyId, userId, token);
        assertNotNull(result, "TEST_COMPANY::addManager | Failed to add manager");
        assertTrue(
                result.getEditorGroups().stream().anyMatch(eg -> eg.getMembers().contains(userId)),
                "TEST_COMPANY::addManager | User, {userId}, is not in readers group.".replace("{userId}", userId)
        );

        return result;
    }

    @Override
    public void getAllCompanyMemberPolicies(String token) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::getAllCompanyMemberPolicies");

        InputOptions pub = CompanyMemberPolicy.PUBLIC;
        InputOptions reg = CompanyMemberPolicy.REGISTRATION_REQUIRED;
        InputOptions auth = CompanyMemberPolicy.AUTHORIZATION_REQUIRED;

        List<InputOptions> result = apiCompany.getAllCompanyMemberPoliciesWithToken(token);
        assertNotNull(result);
        assertTrue(result.stream().anyMatch(policy -> pub.getLabel().equals(policy.getLabel())));
        assertTrue(result.stream().anyMatch(policy -> reg.getLabel().equals(policy.getLabel())));
        assertTrue(result.stream().anyMatch(policy -> auth.getLabel().equals(policy.getLabel())));
    }
}
