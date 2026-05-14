package com.laetienda.utils.service.api;

import com.laetienda.lib.options.CompanyMemberPolicy;
import com.laetienda.lib.options.InputOptions;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class ApiCompanyImplementation extends ApiRestClientImplementation implements ApiCompany{
    private final static Logger log = LoggerFactory.getLogger(ApiCompanyImplementation.class);

    private final RestClient client;

    @Value("${api.company.create.uri}")
    private String createCompanyFidUri;

    @Value("${api.company.find.uri}")
    private String findCompanyUri;

    @Value("${api.company.create.uri}")
    private String createCompanyUri;

    @Value("${api.company.delete.uri}")
    private String deleteCompanyUri;

    @Value("${api.company.isValid.uri}")
    private String isValidCompanyUri;

    @Value("${api.company.find.uri}")
    private String fidCompanyUri;

    @Value("${api.company.findByName.uri}")
    private String findCompanyByNameUri;

    @Value("${api.company.update.uri.name}")
    private String updateCompanyNameUri;

    @Value("${api.company.update.uri.description}")
    private String updateCompanyUriDescription;

    @Value("${api.company.update.uri.content}")
    private String updateCompanyUriContent;

    @Value("${api.company.member.add.uri}")
    private String addCompanyMemberUri;

    @Value("${api.company.member.find.uri}")
    private String findCompanyMemberUri;

    @Value("${api.company.member.update.uri}")
    private String updateCompanyMemberUri;

    @Value("${api.company.member.delete.uri}")
    private String deleteCompanyMemberUri;

    @Value("${api.company.policy.all.uri}")
    private String findAllCompanyMemberPoliciesUri;

    public ApiCompanyImplementation(
            RestClient restClient
    ) {
        super(restClient);
        this.client = restClient;
    }

    @Override
    public Company find(Long id) throws HttpStatusCodeException {
        return super.get(Company.class, null, findCompanyUri, id);
    }

    @Override
    public Company find(Long id, String token) throws HttpStatusCodeException {
        return super.get(Company.class,
                a -> a.put("jwtToken", token),
                findCompanyUri, id);
    }

    @Override
    public Company createCompany(Company company) throws HttpStatusCodeException {
        return super.post(Company.class, company, null, createCompanyUri);
    }

    @Override
    public Company createCompany(Company company, String token) throws HttpStatusCodeException {
        return super.post(Company.class, company,
                attributes -> attributes.put("jwtToken", token),
                createCompanyUri
                );
    }

    @Override
    public void deleteCompany(String companyId) throws HttpStatusCodeException {
        super.delete(
                null,
                deleteCompanyUri,
                companyId
        );
    }

    @Override
    public void deleteCompany(Long companyId, String token) throws HttpStatusCodeException {
        super.delete(
                attrs -> attrs.put("jwtToken", token),
                deleteCompanyUri,
                companyId, token);
    }

    @Override
    public String isValid(Long id, String token) throws HttpStatusCodeException {
        return super.get(
                a -> a.put("jwtToken", token),
                isValidCompanyUri, id
        );
    }

    @Override
    public Company findByName(String name, String token) throws HttpStatusCodeException {
        return super.get(
                Company.class,
                a -> a.put("jwtToken", token),
                findCompanyByNameUri, name
        );
    }

    @Override
    public Company updateName(Long id, String newName, String token) throws HttpStatusCodeException {
        return super.put(
                Company.class,
                a -> a.put("jwtToken", token),
                updateCompanyNameUri, id, newName
        );
    }

    @Override
    public Company updateDescription(Long id, String description, String token) throws HttpStatusCodeException {
        return super.put(
                Company.class,
                description,
                a -> a.put("jwtToken", token),
                updateCompanyUriDescription, id
        );
    }

    @Override
    public Company updateContent(Long id, Map<String, String> body, String token) throws HttpStatusCodeException {
        return super.put(
                Company.class,
                body,
                a -> a.put("jwtToken", token),
                updateCompanyUriContent, id
        );
    }

    @Override
    public Member addMember(Long companyId, String userId, String token) throws HttpStatusCodeException {
        return super.put(
                Member.class,
                a -> a.put("jwtToken", token),
                addCompanyMemberUri, companyId, userId
        );
    }

    @Override
    public Member findMember(Long companyId, String userId, String token) throws HttpStatusCodeException {
        return super.get(
                Member.class,
                a -> a.put("jwtToken", token),
                findCompanyMemberUri, companyId, userId
        );
    }

    @Override
    public Member updateMember(Member body, String token) throws HttpStatusCodeException {
        return super.put(
                Member.class,
                body,
                a -> a.put("jwtToken", token),
                updateCompanyMemberUri
        );
    }

    @Override
    public void deleteMember(Long companyId, String userId, String token) throws HttpStatusCodeException {
        super.delete(
                a -> a.put("jwtToken", token),
                deleteCompanyMemberUri, companyId, userId
        );
    }

    @Override
    public List<InputOptions> getAllCompanyMemberPolicies() throws HttpStatusCodeException {
        return getAllCompanyMemberPolicies(null);
    }

    @Override
    public List<InputOptions> getAllCompanyMemberPoliciesWithToken(String token) throws HttpStatusCodeException {
        return getAllCompanyMemberPolicies(a -> a.put("jwtToken", token));
    }

    @Override
    public List<InputOptions> getAllCompanyMemberPoliciesWithClientRegistrationId(String clientRegistrationId) throws HttpStatusCodeException {
        return getAllCompanyMemberPolicies(clientRegistrationId(clientRegistrationId));
    }

    private List<InputOptions> getAllCompanyMemberPolicies(Consumer<Map<String, Object>> attributes) throws HttpStatusCodeException {
        List<CompanyMemberPolicy> resp = client.get().uri(findAllCompanyMemberPoliciesUri)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<CompanyMemberPolicy>>() {})
                .getBody();

        if(resp == null || resp.isEmpty()) {
            throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "");
        }

        return resp.stream().map(InputOptions.class::cast).collect(Collectors.toList());
    }
}
