package com.laetienda.company.controller;

import com.laetienda.company.service.CompanyService;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Member;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.company.folder}") //api/v0/company
public class MemberController {
    private static final Logger log = LoggerFactory.getLogger(MemberController.class);

    @Autowired private CompanyService service;

    @GetMapping("${api.company.member.find.file}") //api/v0/company/member/find/{companyId}/{userId}
    public ResponseEntity<Member> find(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException {
        log.info("MEMBER_CONTROLLER::find. $company: {} | $user: {}", companyId, userId);
        return ResponseEntity.ok(service.findMemberByIds(companyId, userId));
    }

    @PutMapping("${api.company.member.add.file}") //api/v0/company/member/add/{companyId}/{userId}
    public ResponseEntity<Member> addMember(@PathVariable String companyId, @PathVariable String userId) throws NotValidCustomException{
        log.info("MEMBER_CONTROLLER::addMember. $companyId: {} | $userId: {}", companyId, userId);
        return ResponseEntity.ok(service.addMember(companyId, userId));
    }

    @PutMapping("${api.company.member.update.file}")
    public ResponseEntity<Member> update(@RequestBody @Valid Member member) throws NotValidCustomException{
        log.info("MEMBER_CONTROLLER::modify. $memberId: {}", member.getId());
        return ResponseEntity.ok(service.updateMember(member));
    }
}
