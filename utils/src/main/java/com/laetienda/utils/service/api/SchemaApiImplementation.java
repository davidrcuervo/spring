package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class SchemaApiImplementation extends ApiClientImplementation implements SchemaApi {
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
    public <T> ResponseEntity<T> create(Class<T> clazz , DbItem item) throws HttpClientErrorException {
        String address = String.format("%s/%s", schemaUri, env.getProperty("api.schema.create"));
        String encodedClazzName = Base64.getUrlEncoder().encodeToString(clazz.getName().getBytes(StandardCharsets.UTF_8));
        log.trace("SCHEMA_API::create $item.owner: {}, $address: {}, $ecodedClazz: {}", item.getOwner(), address, encodedClazzName);
        return getRestClient().post()
                .uri(address, getPort(), encodedClazzName)
                .body(item)
                .retrieve().toEntity(clazz);
    }

    @Override
    public <T> ResponseEntity<T> find(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        String address = String.format("%s/%s", schemaUri, env.getProperty("api.schema.find"));
        String encodedClazzName = Base64.getUrlEncoder().encodeToString(clazz.getName().getBytes(StandardCharsets.UTF_8));
        log.trace("SCHEMA_API::find $address: {}, $ecodedClazz: {}", address, encodedClazzName);

        return getRestClient().post()
                .uri(address, getPort(), encodedClazzName)
                .body(body)
                .retrieve().toEntity(clazz);
    }

    @Override
    public <T> ResponseEntity<String> delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException {
        String address = String.format("%s/%s", schemaUri, env.getProperty("api.schema.delete"));
        String encodedClazzName = Base64.getUrlEncoder().encodeToString(clazz.getName().getBytes(StandardCharsets.UTF_8));
        log.trace("SCHEMA_API::delete $address: {}, $ecodedClazz: {}", address, encodedClazzName);

        return getRestClient().post()
                .uri(address, getPort(), encodedClazzName)
                .body(body)
                .retrieve().toEntity(String.class);
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
