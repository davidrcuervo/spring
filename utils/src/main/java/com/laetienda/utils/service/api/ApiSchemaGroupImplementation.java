package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laetienda.model.schema.DbGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

@Component
public class ApiSchemaGroupImplementation extends ApiRestClientImplementation implements ApiSchemaGroup {
    private final static Logger log =  LoggerFactory.getLogger(ApiSchemaGroupImplementation.class);

    private final RestClient client;
    private final ObjectMapper json;

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

    public ApiSchemaGroupImplementation(
            RestClient restClient,
            ObjectMapper objectMapper
            ) {
        this.client = restClient;
        this.json = objectMapper;
    }

    @Override
    public List<DbGroup> findAll() throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup find(Long groupId) throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup findByName(String name) throws HttpStatusCodeException {

        var temp = client.get().uri(uriAddressFindByName, name)
                .accept(MediaType.APPLICATION_JSON);

        if(super.getJwtToken() != null){
            temp.header(HttpHeaders.AUTHORIZATION, "Bearer " + super.getJwtToken());
        }

        return temp.retrieve().toEntity(DbGroup.class).getBody();
    }

    @Override
    public DbGroup update(Long groupId, Map<String, String> body) throws HttpStatusCodeException {
        try {
            var temp = client.put().uri(uriAddressUpdate, groupId)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json.writeValueAsBytes(body));

            if(super.getJwtToken() != null){
                temp.header(HttpHeaders.AUTHORIZATION, "Bearer " + super.getJwtToken());
            }

            return temp.retrieve().toEntity(DbGroup.class).getBody();

        } catch (JsonProcessingException e) {
            log.warn("API_SCHEMA_GROUP::update {}", e.getMessage());
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void delete(Long groupId) throws HttpStatusCodeException {
        var  temp = client.delete().uri(uriAddressDelete, groupId);

        if(super.getJwtToken() != null){
            temp.header(HttpHeaders.AUTHORIZATION, "Bearer " + super.getJwtToken());
        }

        temp.retrieve().toBodilessEntity();
    }

    @Override
    public List<DbGroup> getOrphans() throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup addMember(Long groupId, String userId) throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public DbGroup removeMember(Long groupId, String userId) throws HttpStatusCodeException {
        throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
    }
}
