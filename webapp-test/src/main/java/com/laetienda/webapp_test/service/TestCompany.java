package com.laetienda.webapp_test.service;

import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import com.laetienda.webapp_test.repository.TestCompanyRepo;
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
    private final TestCompanyRepo testCompanyRepo;

    public TestCompany(
            UtilsBox utilsBox,
            TestCompanyRepo testCompanyRepo
    ) {
        this.utils = utilsBox;
        this.testCompanyRepo = testCompanyRepo;
    }

    public void run(){

        TestUserDto[] users = utils.getTestUsers(3, "testCompanyApi");
        log.info("TEST_API_COMPANY::run | Starting test");

        try{
            Company comp = testCompanyRepo.create(
                    "Test Company - Name",
                    "tc-name",
                    CompanyMemberPolicy.PUBLIC, users[1]
            );
            String flag = testCompanyRepo.isValid(comp.getId(), users[1].getToken());
            comp = testCompanyRepo.find(comp.getId(), users[1].getToken());
            comp = testCompanyRepo.findByVanityUrl(comp.getVanityUrl(), users[1].getToken());
            comp = testCompanyRepo.findByName(comp.getName(), users[1].getToken());
            comp = testCompanyRepo.updateName(comp.getId(), "newName", users[1].getToken());
            comp = testCompanyRepo.updateDescription(comp.getId(), "description", users[1].getToken());
            Member memb2 = testCompanyRepo.addMember(comp.getId(), users[2].getUserId(), users[1].getToken());
            comp = testCompanyRepo.updateContent(comp.getId(), Map.of("owner", memb2.getId().toString()), users[1].getToken());
            Member memb1 = testCompanyRepo.findMember(comp.getId(), users[1].getUserId(), users[2].getToken());
            memb1.setStatus(CompanyMemberStatus.BLOCKED);
            memb1 = testCompanyRepo.updateMember(memb1, users[2].getToken());
            testCompanyRepo.deleteMember(comp.getId(), users[1].getUserId(), users[1].getToken());
            testCompanyRepo.delete(comp.getId(), users[2].getToken());
            testCompanyRepo.getAllCompanyMemberPolicies(users[0].getToken());
            this.testFindAll(users);

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

    private void testFindAll(TestUserDto[] users) throws HttpStatusCodeException, AssertionError {
        log.debug("TEST_API_COMPANY::testFindAll | Starting test");

        Company comp1 = testCompanyRepo.create(
                "Api Test Company 1 - Find All",
                "apc-fa1",
                CompanyMemberPolicy.PUBLIC,
                users[1]
        );
        Company comp2 = testCompanyRepo.create(
                "Api Test Company 2 - Find All",
                "apc-fa2",
                CompanyMemberPolicy.PUBLIC,
                users[2]
        );
        Company comp3 = testCompanyRepo.create(
                "Api Test Company 3 - Find All",
                "apc-fa3",
                CompanyMemberPolicy.PUBLIC,
                users[3]
        );

        //add user1 as manager to company2
        testCompanyRepo.addMember(comp2.getId(), users[1].getUserId(), users[1].getToken());
        testCompanyRepo.addManager(comp2.getId(), users[1].getUserId(), users[2].getToken());

        //add user1 as member to company3
        testCompanyRepo.addMember(comp3.getId(), users[1].getUserId(), users[1].getToken());

        //test find all
        List<Company> result = testCompanyRepo.findAll(Map.of("manager", ""), users[1].getToken());
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(comp1.getId())));
        assertTrue(result.stream().anyMatch(c -> c.getId().equals(comp2.getId())));
        assertTrue(result.stream().noneMatch(c -> c.getId().equals(comp3.getId())));

        testCompanyRepo.delete(comp1.getId(), users[1].getToken());
        testCompanyRepo.delete(comp2.getId(), users[2].getToken());
        testCompanyRepo.delete(comp3.getId(), users[3].getToken());

        log.debug("TEST_API_COMPANY::testFindAll | Test completed successfully");
    }
}
