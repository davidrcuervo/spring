package com.laetienda.webapp_test.test;

import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import com.laetienda.model.user.TestUserDto;
import com.laetienda.utils.lib.UtilsBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;

@Service
public class TestCompany {
    private final static Logger log = LoggerFactory.getLogger(TestCompany.class);

    private final UtilsBox utils;
    private final com.laetienda.webapp_test.testApi.TestCompanyApi testCompanyApi;

    public TestCompany(
            UtilsBox utilsBox,
            com.laetienda.webapp_test.testApi.TestCompanyApi testCompanyApi
    ) {
        this.utils = utilsBox;
        this.testCompanyApi = testCompanyApi;
    }

    public void run(){

        TestUserDto[] users = utils.getTestUsers(2, "testCompanyApi");
        log.info("TEST_API_COMPANY::run | Starting test");

        try{
            Company comp = testCompanyApi.create("name", CompanyMemberPolicy.PUBLIC, users[1]);
            String flag = testCompanyApi.isValid(comp.getId(), users[1].getToken());
            comp = testCompanyApi.find(comp.getId(), users[1].getToken());
            comp = testCompanyApi.findByName(comp.getName(), users[1].getToken());
            comp = testCompanyApi.updateName(comp.getId(), "newName", users[1].getToken());
            comp = testCompanyApi.updateDescription(comp.getId(), "description", users[1].getToken());
            Member memb2 = testCompanyApi.addMember(comp.getId(), users[2].getUserId(), users[1].getToken());
            comp = testCompanyApi.updateContent(comp.getId(), Map.of("owner", memb2.getId().toString()), users[1].getToken());
            Member memb1 = testCompanyApi.findMember(comp.getId(), users[1].getUserId(), users[2].getToken());
            memb1.setStatus(CompanyMemberStatus.BLOCKED);
            memb1 = testCompanyApi.updateMember(memb1, users[2].getToken());
            testCompanyApi.deleteMember(comp.getId(), users[1].getUserId(), users[1].getToken());
            testCompanyApi.delete(comp.getId(), users[2].getToken());

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
}
