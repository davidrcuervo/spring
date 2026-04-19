package com.laetienda.company.service;

import com.laetienda.company.repository.CompanyRepository;
import com.laetienda.company.repository.FriendRepository;
import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.CompanyMemberStatus;
import com.laetienda.lib.options.DbServiceAccessPolicy;
import com.laetienda.lib.options.DbUserAccessPolicy;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Friend;
import com.laetienda.model.company.Member;
import com.laetienda.model.schema.DbGroup;
import com.laetienda.utils.service.api.ApiSchema;
import com.laetienda.utils.service.api.ApiSchemaGroup;
import com.laetienda.utils.service.api.ApiUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CompanyServiceImplementation implements CompanyService{
    private final static Logger log = LoggerFactory.getLogger(CompanyServiceImplementation.class);

    private final Validator validator;
    private final HttpServletRequest request;
    private final CompanyRepository repo;
    private final FriendRepository repoFriend;
    private final ApiUser apiUser;
//    private final ApiSchemaGroup apiSchemaGroup;
    private final ApiSchema apiSchema;


    public CompanyServiceImplementation(
            Validator validator,
            HttpServletRequest httpServletRequest,
            CompanyRepository companyRepository,
            FriendRepository friendRepository,
//            ApiSchemaGroup apiSchemaGroup,
            ApiUser apiUser,
            ApiSchema apiSchema
    ) {
        this.validator = validator;
        this.request = httpServletRequest;
        this.repo = companyRepository;
        this.repoFriend = friendRepository;
        this.apiUser = apiUser;
        this.apiSchema = apiSchema;
//        this.apiSchemaGroup = apiSchemaGroup;
    }

    @Override
    public Company create(@NotNull Company company) throws NotValidCustomException {
        String userId = apiUser.getCurrentUserId();
        log.debug("COMPANY_SERVICE::create. $company: {}. $currentUserId: {}", company.getName(), userId);

        try {
            Company temp = repo.findByName(company.getName());
            String message = String.format("Company %s already exists.", company.getName());
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "company");

        }catch(HttpStatusCodeException e){
            if(e.getStatusCode() == HttpStatus.NOT_FOUND){

                Company result = repo.create(company);

                Member member = new Member(result, userId, CompanyMemberStatus.ACCEPTED);
                repo.acceptMember(member);
                repo.addMember(member);

                return result;

            }else{
                throw e;
            }
        }
    }

    @Override
    public Long isCompanyValid(String companyId) throws HttpStatusCodeException {
        log.debug("COMPANY_SERVICE::isCompanyValid. $companyId: {}", companyId);

        try {
            Long id = Long.parseLong(companyId);
            return repo.isCompanyValid(id);
        }catch(NumberFormatException e){
            String message = String.format("COMPANY_SERVICE::isCompanyValid. companyId must be number of Long format. $companyId: %s", companyId);
            log.warn("COMPANY_SERVICE::companyId. {}", message);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
        }
    }

    @Override
    public Company find(String strId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::find. $id: {}", strId);

        Long cid = isCompanyValid(strId);

        String loggedUserId = request.getUserPrincipal().getName();
        Member loggedMember = this.findMemberByIds(strId, loggedUserId);

        if(!loggedMember.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
            String m = "Member does not have ACCEPTED status" +
                    " | $status: %s" +
                    " | $userId: %s";
            String message = String.format(m, loggedMember.getStatus(), loggedUserId);
            log.warn("COMPANY_SERVICE::find {}", message);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);
        }

        return repo.find(cid);
    }

    @Override
    public Company findByName(String name) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findByName. $name: {}", name);
        return repo.findByName(name);
    }

    @Override
    public void delete(String companyId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::delete. $id: {}", companyId);
        Long cid = isCompanyValid(companyId);

        Company comp = repo.find(cid);

        Member ownerMember = findMemberByIds(companyId, comp.getOwner());
        repo.removeMember(ownerMember);

        List<Member> members = findAllMembers(cid);
        for(Member member : members){
            deleteMember(cid, member.getUserId());
        }

        repo.delete(comp);
    }

    @Override
    public void deleteMember(String companyId, String userId) throws NotValidCustomException {
        Long cid = isCompanyValid(companyId);
        deleteMember(cid, userId);
    }

    @Override
    public void deleteMember(Long companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::removeMember. $company: {} | $userId: {}", companyId, userId);

        Company comp = repo.find(companyId);

        if(comp.getOwner().equals(userId)){
            String m = "Member can't be removed because is owner of the company" +
                    " | $company: %s" +
                    " | $userId: %s";
            String mess = String.format(m, comp.getName(), userId);
            log.warn("COMPANY_SERVICE::removeMember. {}", mess);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, mess);
        }

        //Remove all friendships of member
        Member member = findMemberByIds(companyId.toString(), userId);
        for(Friend f : repoFriend.findAll(companyId, userId)){
            repoFriend.delete(f);
        }

        repo.removeMember(member);
    }

    @Override
    public Member addMember(String companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::addMember. $company: {} | $userId: {}", companyId, userId);

        Long cid = isCompanyValid(companyId);
        String uid = apiUser.isUserIdValid(userId);
        String loggedUserId = request.getUserPrincipal().getName();

        Company company = repo.findNoJwt(cid);
        CompanyMemberPolicy policy = company.getMemberPolicy();
        CompanyMemberStatus status = CompanyMemberStatus.REQUESTED;

        List<Member> temp = repo.findMemberByUserIdNoJwt(cid, userId);

        if(temp != null && !temp.isEmpty()){
            String message = String.format("Member already exists. $companyId: %d | $userId: %s", cid, uid);
            log.warn("COMPANY_SERVICE::addMember. $message: {}", message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "member");
        }

        if(!(loggedUserId.equals(userId) || isCompanyManager(loggedUserId,  company))){
            String m = "Member must be added only by managers or self user requesting membrane to company" +
                    " | $companyId: %d" +
                    " | $memberUserId: %s" +
                    " | $loggedInUserId: %s";
            String message = String.format(m, cid, userId, uid);
            log.warn("COMPANY_SERVICE::addMember. $message: {}", message);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);
        }

        Member member = new Member(company, uid, status);
        company.addMember(member);

        if(policy == CompanyMemberPolicy.PUBLIC || policy == CompanyMemberPolicy.REGISTRATION_REQUIRED) {
            repo.acceptMember(member);
        }

        return repo.addMember(member);
    }

    @Override
    public Member findMemberByIds(String companyId, String userId) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findMemberByIds. $companyId: {} | $userId: {}", companyId, userId);

        String uid = apiUser.isUserIdValid(userId);
        Long cid = isCompanyValid(companyId);

        List<Member> result = repo.findMemberByUserId(cid, uid);

        if (result == null || result.isEmpty()) {
            String message = String.format("User is not member of company. $companyId: %d | $user: '%s", cid, uid);
            log.warn("COMPANY_SERVICE::findMemberByIds. {}", message);
            throw new NotValidCustomException(message, HttpStatus.NOT_FOUND, "member");

        } else if (result.size() > 1) {
            String message = String.format("COMPANY_SERVICE::findMemberByIds. There are more than one member in company with same userId. $companyId: %s | $user: %s", cid, uid);
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.INTERNAL_SERVER_ERROR, "member");
        }

        return result.getFirst();
    }

    @Override
    public List<Member> findAllMembers(Long cid) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::findAllMembers. $cid: {}", cid);
        return repo.findAllMembers(cid);
    }

    @Override
    public Member updateMember(Member member) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::updateMember. $memberId: {}", member.getId());

        Member temp = apiSchema.findById(Member.class, member.getId()).getBody();
        String currentUserId = apiUser.getCurrentUserId();

        if(temp == null){
            String message = String.format("Member does not exist. $memberId: %d", member.getId());
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "member");
        }

        if(!temp.getUserId().equals(member.getUserId())){
            String message = String.format("User of member can't be modified. $userId: %s", member.getUserId());
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "member");
        }

        if(!temp.getCompany().getId().equals(member.getCompany().getId())){
            String message = String.format("Company of member can't be modified. $companyId: %s", member.getCompany().getId());
            log.error(message);
            throw new NotValidCustomException(message, HttpStatus.BAD_REQUEST, "member");
        }

        if(!temp.getStatus().equals(member.getStatus()))
            updateMemberStatus(temp, member);

        return repo.updateMember(member);
    }

    @Override
    public Company updateName(String companyId, String value) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::updateName. $companyId: {} | $value: {}", companyId, value);

        if(!isCompanyNameValid(value)) {
            String message = String.format("Company name has been modified but new name is not valid. $companyName: %s", value);
            log.warn(message);
            throw new NotValidCustomException(message, HttpStatus.FORBIDDEN, "company");
        }

        Long cid = isCompanyValid(companyId);
        Company temp = repo.find(cid);

        return repo.updateCompanyName(value, temp);
    }

    @Override
    public Company updateDescription(String companyId, String value) throws NotValidCustomException {
        log.debug("COMPANY_SERVICE::updateDescription. $companyId: {} | $value: {}", companyId, value);
        Long cid = isCompanyValid(companyId);
        Company temp = repo.find(cid);
        temp.setDescription(value);

        Set<ConstraintViolation<Company>> violations = validator.validate(temp);
        if (!violations.isEmpty()) {
            StringBuilder message = new StringBuilder();

            for(ConstraintViolation<Company> violation : violations) {
                message.append(violation.getMessage()).append(", ");
                log.warn(violation.getMessage());
            }

            throw new  NotValidCustomException(message.toString(), HttpStatus.BAD_REQUEST, "company");
        }

        return repo.updateCompany(temp);
    }

    @Override
    public Company updateCompanyContent(String companyId, Map<String, String> body) throws HttpStatusCodeException {
        log.debug("COMPANY_SERVICE::updateCompanyContent. $companyId: {}", companyId);

        Long cid = isCompanyValid(companyId);
        Company temp = repo.find(cid);
        String userId = apiUser.getCurrentUserId();

        ifNotCompanyManagerThrowUnauthorizedException(temp, userId);

        body.forEach((key, value) -> {
            log.trace("COMPANY_SERVICE::updateCompanyContent. key: {}, value: {}", key, value);

            if(key.equals("owner")) {
                updateCompanyOwner(temp, value);

            }else{
                String m = "It is not possible to update that parameter." +
                        " | $parameter: %s" +
                        " | $value: %s" +
                        " | $companyId: %s";
                String message = String.format(m, key, value, companyId);
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
            }
        });

        repo.updateCompany(temp);
        return repo.find(cid);
    }

    private void updateCompanyOwner(Company temp, String value) throws HttpStatusCodeException{
        log.trace("COMPANY_SERVICE::updateCompanyOwner. $companyId: {} | $oldOwner: {} | $newMemberId: {}", temp.getId(), temp.getOwner(), value);

        Member member = findMemberIfErrorThrowException(value);
        temp.setOwner(member.getUserId());

        repo.updateCompanyOwner(member);
    }

    private void updateMemberStatus(Member old, Member member) throws HttpStatusCodeException {
        String currentUserId = request.getUserPrincipal().getName();
        log.trace("COMPANY_SERVICE::updateMemberStatus. $status: {} | $currentUserId: {}", member.getStatus(), currentUserId);

        if(member.getStatus().equals(CompanyMemberStatus.ACCEPTED)
                && (old.getStatus().equals(CompanyMemberStatus.BLOCKED) || old.getStatus().equals(CompanyMemberStatus.REQUESTED))
        ) {
            if(!isCompanyManager(currentUserId, old.getCompany())){
                String m = String.format("Only manger can unblock or accept another member." +
                        " | $status: %s ", member.getStatus().toString());
                log.warn("COMPANY_SERVICE::updateMemberStatus. {}", m);
                throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, m);
            }

            repo.acceptMember(member);
        }

        if(old.getCompany().getOwner().equals(member.getUserId())
                && !member.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
            String m = String.format("Owner of the company can't be blocked. " +
                    "$companyName: %s | " +
                    "$memberStatus: %s | " +
                    "$userId: %s",
                    old.getCompany().getName(),
                    member.getStatus().toString(),
                    member.getUserId());
            log.warn("COMPANY_SERVICE::updateMemberStatus. {}", m);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, m);
        }
      }

    @Override
    public Company addManager(String companyId, String userId) throws HttpStatusCodeException {
        log.debug("COMPANY_SERVICE::addManager. $companyId: {} | $userId: {}", companyId, userId);

        Long cid = isCompanyValid(companyId);
        Company temp = repo.find(cid);

        canEditIfNotThrowException(temp);
        Member member = findMemberIfInvalidThrowException(temp, userId);

        return repo.addManager(member);

//        String currentUserId = apiUser.getCurrentUserId();

//        DbGroup managers = getManagersGroup(temp);
//        apiSchemaGroup.addMember(managers.getId(), userId);

//        if(!isCompanyManager(currentUserId, temp)){
//            String message = String.format("User, %s, is not manager or owner of the company.", currentUserId);
//            log.warn("COMPANY_SERVICE::addManager. {}", message);
//            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, message);
//        }

//        if(!isValidMember(temp, userId)){
//            String m = "User is not valid member of the company." +
//                    " | $company: %s" +
//                    " | $userId: %s";
//            String mess = String.format(m, temp.getName(), userId);
//            log.warn("COMPANY_SERVICE::addManager. {}", mess);
//            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, mess);
//        }

//        if(managers.getMembers().contains(userId)){
//            String message = String.format("User, %s, is already manager of the company.", userId);
//            log.info("COMPANY_SERVICE::addManager. {}", message);
//            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, message);
//        }
//        return repo.find(temp.getId());
    }

    private Boolean isCompanyNameValid(String value) throws NotValidCustomException{
        log.debug("COMPANY_SERVICE::isCompanyNameValid. $companyName: {}", value);

        try{
            Company temp = repo.findByNameNoJwt(value);
            return false;
        }catch(HttpStatusCodeException e){
            if(e.getStatusCode().equals(HttpStatus.NOT_FOUND)){
                return true;
            }else{
                throw e;
            }
        }
    }

    private void modifyCompanyOwner(Company temp, Company company) throws NotValidCustomException {

        String newOwnerUserId = company.getOwner();
        List<Member> members = findAllMembers(temp.getId());

        for(Member member : members){
            if(!member.getEditors().contains(newOwnerUserId)) {
                member.addEditor(newOwnerUserId);
                repo.updateMember(member);
            }
        }
    }

    private void ifNotCompanyManagerThrowUnauthorizedException(Company comp, String userId) throws HttpStatusCodeException{
        if(!isCompanyManager(userId, comp)){
            String m = "Member does not have manager privileges for the company." +
                    " | $company: %s " +
                    " | $userId: %s";
            String mess = String.format(m, comp.getName(), userId);
            log.warn("COMPANY_SERVICE::ifNotCompanyManager. {}", mess);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, mess);
        }
    }

    private Member findMemberIfErrorThrowException(String memberId) throws HttpStatusCodeException {
        try{
            Long mid = Long.parseLong(memberId);
            return findMemberIfErrorThrowException(mid);
        }catch(NumberFormatException e){
            String m = "MemberId is not valid member id. | $value: %s";
            String mess = String.format(m, memberId);
            log.warn("COMPANY_SERVICE::ifNotValidMemberIdThrowUnauthorizedException.. {}", mess);
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, mess);
        }
    }

    private Member findMemberIfErrorThrowException(Long memberId) throws HttpStatusCodeException{
        Member member = repo.findMemberById(memberId);

        if(member == null){
            String m = "Member does not exist. | $memberId: %s";
            String mess = String.format(m, memberId);
            log.warn("COMPANY_SERVICE::ifNotValidMemberIdThrowUnauthorizedException. {}", mess);
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, mess);
        }

        if(!member.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
            String m = "Member status is not valid. | $memberId: %s | $status: %s";
            String mess = String.format(m, memberId, member.getStatus());
            log.warn("COMPANY_SERVICE::ifNotValidMemberIdThrowUnauthorizedException. {}", mess);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, mess);
        }

        return member;
    }

    private void canEditIfNotThrowException(Company company) throws HttpStatusCodeException{
        String loggedUserId = request.getUserPrincipal().getName();

        if(!isCompanyManager(loggedUserId, company)){
            String m = "Logged user can't edit the company." +
                    " | $company: %s" +
                    " | $userId: %s";
            String mess = String.format(m, company.getName(), loggedUserId);
            log.warn("COMPANY_SERVICE::canEditIfNotThrowException.. {}", mess);
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, mess);
        }
    }

    private boolean isCompanyManager(String currentUserId, Company company){
        if(!isValidMember(company, currentUserId))
            return false;

        if(company.getOwner().equals(currentUserId))
            return true;

        if(company.getEditors().contains(currentUserId))
            return true;

        return company.getEditorGroups().stream()
                .anyMatch(g -> g.getMembers().contains(currentUserId));
    }

    private Member findMemberIfInvalidThrowException(Company company, String userId) throws HttpStatusCodeException{
        log.debug("COMPANY_SERVICE::findMemberIfInvalidThrowException. {}", userId);

        String m = "User is not valid member of the company." +
                " | $company: %s" +
                " | $userId: %s";
        String mess = String.format(m, company.getName(), userId);

        List<Member> members = repo.findMemberByUserId(company.getId(), userId);

        if (members == null || members.size() != 1){
            log.warn("COMPANY_SERVICE::addManager. {}", mess);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, mess);
        }

        Member result  = members.getFirst();
        if(!result.getStatus().equals(CompanyMemberStatus.ACCEPTED)){
            log.warn("COMPANY_SERVICE::addManager. {}", mess);
            throw new HttpClientErrorException(HttpStatus.FORBIDDEN, mess);
        }

        return result;
    }

    private boolean isValidMember(Company comp, String userId){

        try {
            List<Member> members = repo.findMemberByUserId(comp.getId(), userId);

            if (members == null || members.size() != 1)
                return false;

            return members.getFirst().getStatus().equals(CompanyMemberStatus.ACCEPTED);
        }catch(HttpStatusCodeException e){
            return false;
        }
    }
}