package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class SchemaApiImplementation extends ApiClientServiceImplementation implements SchemaApi {
    private static final Logger log = LoggerFactory.getLogger(SchemaApiImplementation.class);

    @Autowired private Environment env;

    @Value("${api.schema.uri}")
    private String schemaUri;

    @Override
    public ResponseEntity<String> helloAll() throws HttpClientErrorException {
        String address = String.format("%s/%s", schemaUri, env.getProperty("api.schema.helloAll"));
        log.trace("SCHEMA_API::helloAll $address: {}", address);
        return getRestClient().post().uri(address, getPort()).retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> helloUser() throws HttpClientErrorException {
        String address = String.format("%s/%s", schemaUri, env.getProperty("api.schema.helloUser"));
        log.trace("SCHEMA_API::helloUser $address: {}", address);
        return getRestClient().post().uri(address, getPort()).retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> helloValidatedUser() throws HttpClientErrorException {
        String address = String.format("%s/%s",schemaUri, env.getProperty("api.schema.helloValidatedUser"));
        log.trace("SCHEMA_API::helloValidateUser. $address: {}", address);
        return getRestClient().post().uri(address, getPort()).retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<DbItem> create(DbItem item) throws HttpClientErrorException {
        String address = String.format("%s/%s", schemaUri, env.getProperty("api.schema.create"));
        log.trace("SCHEMA_API::create $item.owner: {}, $item.group: {}, $address: {}", item.getOwner(), item.getGrupo(), address);
        return getRestClient().post()
                .uri(address, getPort())
                .body(item)
                .retrieve().toEntity(DbItem.class);
    }

    @Override
    public String getPort(){
        log.trace("SCHEMA_API::getPort $super.port: {}", super.getPort());
        if(super.getPort() == null){
            super.setPort(env.getProperty("server.port"));
        }

        return super.getPort();
    }
}
