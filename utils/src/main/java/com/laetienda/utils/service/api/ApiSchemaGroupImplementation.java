package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.company.Company;
import com.laetienda.model.schema.DbGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

@Component
public class ApiSchemaGroupImplementation extends ApiRestClientImplementation implements ApiSchemaGroup {
    private final static Logger log =  LoggerFactory.getLogger(ApiSchemaGroupImplementation.class);

    @Value("${api.schema.group.uri.findAll}")
    private String uriAddressFindAll;

    @Value("${api.schema.group.uri.findByName}")
    private String uriAddressFindByName;

    @Value("${api.schema.group.uri.update}")
    private String uriAddressUpdate;

    @Value("${api.schema.group.uri.delete}")
    private String uriAddressDelete;

    @Value("${api.schema.group.uri.orphans}")
    private String uriAddressOrphans;

    @Value("${api.schema.group.uri.member.add}")
    private String uriAddressAddMember;

    @Value("${api.schema.group.uri.member.remove}")
    private String uriAddressRemoveMember;

    @Value("${kc.client-registration-id.webapp}")
    private String clientRegistrationId;

    private final RestClient client;

    public ApiSchemaGroupImplementation(
            RestClient restClient,
            ToolBoxService toolBoxService
            ) {
        super(restClient, toolBoxService);
        this.client = restClient;
    }

    @Override
    public List<DbGroup> findAll() throws HttpStatusCodeException {
        return client.get()
                .uri(uriAddressFindAll)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<DbGroup>>() {
                })
                .getBody();
    }

    @Override
    public List<DbGroup> findAll(String token) throws HttpStatusCodeException {
        return client.get()
                .uri(uriAddressFindAll)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(a -> a.put("jwtToken", token))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<DbGroup>>() {
                })
                .getBody();
    }

    @Override
    public List<DbGroup> findAllByService(String clientRegistrationId) throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup find(Long groupId) throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup find(Long groupId, String token) throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup findByName(String name) throws HttpStatusCodeException {

        return client.get()
                .uri(uriAddressFindByName, name)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup findByName(String name, String token) throws HttpStatusCodeException {
        return client.get()
                .uri(uriAddressFindByName, name)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(a -> {a.put("jwtToken", token);})
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup update(Long groupId, Map<String, String> body) throws HttpStatusCodeException {
        return client.put()
                .uri(uriAddressUpdate, groupId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup update(Long groupId, Map<String, String> body, String token) throws HttpStatusCodeException {
        return client.put()
                .uri(uriAddressUpdate, groupId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(a -> {a.put("jwtToken", token);})
                .body(body)
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public void delete(Long groupId) throws HttpStatusCodeException {
        client.delete().uri(uriAddressDelete, groupId)
                .retrieve().toBodilessEntity();
    }

    @Override
    public void delete(Long groupId, String token) throws HttpStatusCodeException {
        client.delete().uri(uriAddressDelete, groupId)
                .attributes(a -> {a.put("jwtToken", token);})
                .retrieve().toBodilessEntity();
    }

    @Override
    public List<DbGroup> getOrphans() throws HttpStatusCodeException {
        return client.get()
                .uri(uriAddressOrphans)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<DbGroup>>() {
                })
                .getBody();
    }

    @Override
    public List<DbGroup> getOrphans(String token) throws HttpStatusCodeException {
        return client.get()
                .uri(uriAddressOrphans)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(a -> {a.put("jwtToken", token);})
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<DbGroup>>() {
                })
                .getBody();
    }

    @Override
    public DbGroup addMember(Long groupId, String userId) throws HttpStatusCodeException {
        return client.put()
                .uri(uriAddressAddMember, groupId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup addMember(Long groupId, String userId, String token) throws HttpStatusCodeException {
        return client.put()
                .uri(uriAddressAddMember, groupId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(a -> {a.put("jwtToken", token);})
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup addMemberByService(Long groupId, String userId) throws HttpStatusCodeException {
        return client.put()
                .uri(uriAddressAddMember, groupId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(clientRegistrationId(clientRegistrationId))
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup removeMember(Long groupId, String userId) throws HttpStatusCodeException {
        return client.delete().uri(uriAddressRemoveMember, groupId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }

    @Override
    public DbGroup removeMember(Long groupId, String userId, String token) throws HttpStatusCodeException {
        return client.delete().uri(uriAddressRemoveMember, groupId, userId)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(a -> {a.put("jwtToken", token);})
                .retrieve()
                .toEntity(DbGroup.class)
                .getBody();
    }
}
