package com.laetienda.webapp_test.service;

import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.webapp_test.repository.TestCompanyRepo;
import com.laetienda.webapp_test.repository.TestSchemaRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Service
public class TestCompany {
    private final static Logger log = LoggerFactory.getLogger(TestCompany.class);

    private final UtilsBox utils;
    private final TestCompanyRepo repo;
    private final TestSchemaRepo repoSchema;

    public TestCompany(
            UtilsBox utilsBox,
            TestCompanyRepo repo,
            TestSchemaRepo schemaRepo
    ) {
        this.utils = utilsBox;
        this.repo = repo;
        this.repoSchema = schemaRepo;
    }

    public void run(){

        TestUserDto[] users = utils.getTestUsers(3, "testCompanyApi");
        log.info("TEST_API_COMPANY::run | Starting test");

        try{
            Company comp = repo.create(
                    "Test Company - Name",
                    "tc-name",
                    CompanyMemberPolicy.PUBLIC, users[1]
            );
            String flag = repo.isValid(comp.getId(), users[1].getToken());
            comp = repo.find(comp.getId(), users[1].getToken());
            comp = repo.findByVanityUrl(comp.getVanityUrl(), users[1].getToken());
            comp = repo.findByName(comp.getName(), users[1].getToken());
            comp = repo.updateName(comp.getId(), "newName", users[1].getToken());
            comp = repo.updateDescription(comp.getId(), "description", users[1].getToken());
            Member memb2 = repo.addMember(comp.getId(), users[2].getUserId(), users[1].getToken());
            comp = repo.updateContent(comp.getId(), Map.of("owner", memb2.getId().toString()), users[1].getToken());
            Member memb1 = repo.findMember(comp.getId(), users[1].getUserId(), users[2].getToken());
            memb1.setStatus(CompanyMemberStatus.BLOCKED);
            memb1 = repo.updateMember(memb1, users[2].getToken());
            repo.deleteMember(comp.getId(), users[1].getUserId(), users[1].getToken());
            repo.delete(comp.getId(), users[2].getToken());
            repo.getAllCompanyMemberPolicies(users[0].getToken());
            this.testFindAll(users);
            this.getMembers(users);
            this.getManagers(users);

            log.info("TEST_API_COMPANY::run | Test completed successfully");
        }catch(HttpStatusCodeException ex){
            log.debug("TEST_COMPANY_API::run $exception: {} | $code: {} | $error: {}", ex.getClass().getSimpleName(), ex.getStatusCode(), ex.getMessage());
            throw ex;

        }catch(AssertionError | Exception ex){
            log.debug("TEST_COMPANY_API::run $exception: {} | $error: {}", ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;

        }finally{
            utils.deleteTestUsers(users);
        }
    }

    private void getMembers(TestUserDto[] users) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_COMPANY::getMembers | Starting test");

        Company comp = repo.create(
                "Test Company - API - Get Members",
                "tc-api-gmembs",
                CompanyMemberPolicy.AUTHORIZATION_REQUIRED,
                users[1]
        );

        Member member2 = repo.addMember(comp.getId(), users[2].getUserId(), users[2].getToken());
        repo.addMember(comp.getId(), users[3].getUserId(), users[3].getToken());

        member2.setStatus(CompanyMemberStatus.ACCEPTED);
        repo.updateMember(member2, users[1].getToken());
        repo.addManager(comp.getId(), users[2].getUserId(), users[1].getToken());

        List<Member> members = repo.getMembers(comp.getId(), null, users[2].getToken());
        assertNotNull(members);
        assertEquals(3, members.size());
        assertTrue(members.stream().anyMatch(
                m -> m.getUserId().equals(users[1].getUserId())
        ));
        assertTrue(members.stream().anyMatch(
                m -> m.getUserId().equals(users[2].getUserId())
        ));
        assertTrue(members.stream().anyMatch(
                m -> m.getUserId().equals(users[3].getUserId())
        ));

        members = repo.getMembers(
                comp.getId(),
                Map.of("status", CompanyMemberStatus.ACCEPTED.name()),
                users[2].getToken()
        );
        assertNotNull(members);
        assertEquals(2, members.size());
        assertTrue(members.stream().anyMatch(
                m -> m.getUserId().equals(users[1].getUserId())
        ));
        assertTrue(members.stream().anyMatch(
                m -> m.getUserId().equals(users[2].getUserId())
        ));
        assertTrue(members.stream().noneMatch(
                m -> m.getUserId().equals(users[3].getUserId())
        ));

        repo.delete(comp.getId(), users[1].getToken());
        log.debug("TEST_COMPANY::getMembers | Test finished successfully!");
    }

    private void getManagers(TestUserDto[] users) throws HttpStatusCodeException, AssertionError{
        log.debug("TEST_COMPANY::getManagers. | Starting test");

        Company comp = repo.create(
                "Test Company - API - Get Managers",
                "tc-api-gmans",
                CompanyMemberPolicy.PUBLIC,
                users[1]
        );

        repo.addMember(comp.getId(), users[2].getUserId(), users[2].getToken());
        repo.addMember(comp.getId(), users[3].getUserId(), users[3].getToken());
        repo.addManager(comp.getId(), users[2].getUserId(), users[1].getToken());

        List<Member> managers = repo.getManagers(comp.getId(), users[2].getToken());
        assertNotNull(managers);
        assertEquals(2, managers.size());
        assertTrue(
                managers.stream().anyMatch(m -> m.getUserId().equals(users[1].getUserId()))
        );
        assertTrue(
                managers.stream().anyMatch(m -> m.getUserId().equals(users[2].getUserId()))
        );
        assertFalse(
                managers.stream().anyMatch(m -> m.getUserId().equals(users[3].getUserId()))
        );

        repo.delete(comp.getId(), users[1].getToken());
        log.debug("TEST_COMPANY::getManagers. | Test finished successfully");
    }

    private void testFindAll(TestUserDto[] users) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_API_COMPANY::testFindAll | Starting test");

        Company comp1 = repo.create(
                "Api Test Company 1 - Find All",
                "apc-fa1",
                CompanyMemberPolicy.PUBLIC,
                users[1]
        );
        Company comp2 = repo.create(
                "Api Test Company 2 - Find All",
                "apc-fa2",
                CompanyMemberPolicy.PUBLIC,
                users[2]
        );
        Company comp3 = repo.create(
                "Api Test Company 3 - Find All",
                "apc-fa3",
                CompanyMemberPolicy.PUBLIC,
                users[3]
        );

        //add user1 as manager to company2
        repo.addMember(comp2.getId(), users[1].getUserId(), users[1].getToken());
        repo.addManager(comp2.getId(), users[1].getUserId(), users[2].getToken());

        //add user1 as member to company3
        repo.addMember(comp3.getId(), users[1].getUserId(), users[1].getToken());

        //test find all
        List<Company> result = repo.findAll(Map.of("manager", ""), users[1].getToken());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(comp1.getId())));
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(comp2.getId())));
        assertTrue(result.stream().noneMatch(c -> c.getId().equals(comp3.getId())));

//        repoSchema.deleteGroups(comp1, users[1].getToken());
        repo.delete(comp1.getId(), users[1].getToken());

//        repoSchema.deleteGroups(comp2, users[2].getToken());
        repo.delete(comp2.getId(), users[2].getToken());

//        repoSchema.deleteGroups(comp3, users[3].getToken());
        repo.delete(comp3.getId(), users[3].getToken());

        log.debug("TEST_API_COMPANY::testFindAll | Test completed successfully");
    }
}
