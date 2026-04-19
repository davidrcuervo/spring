package com.laetienda.utils.service.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.laetienda.model.schema.DbItem;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver;
import org.springframework.util.TypeUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

public abstract class ApiRestClientImplementation implements ApiRestClient {

    private final RestClient client;

    public ApiRestClientImplementation(RestClient restClient) {
        this.client = restClient;
    }

    @Override
    public <T extends DbItem> T get(
            Class<T> responseType,
            Consumer<Map<String, Object>> attributes,
            String address,
            Object... uriVariables

    ) throws HttpStatusCodeException{

        return client.get()
                .uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public String get(
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException{

        return client.get().uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .retrieve()
                .toEntity(String.class)
                .getBody();
    }

    @Override
    public <T extends DbItem> List<T> getList(
            Class<T> responseType,
            Consumer<Map<String, Object>> attributes,
            String address,
            Object... uriVariables

    ) throws HttpStatusCodeException {

        ParameterizedTypeReference<List<T>> typeRef = ParameterizedTypeReference.forType(
                ResolvableType.forClassWithGenerics(List.class, responseType).getType()
        );

        return client.get()
                .uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .retrieve()
                .toEntity(typeRef)
                .getBody();
    }

    @Override
    public <T extends DbItem> T post(
            Class<T> responseType,
            T body,
            Consumer<Map<String, Object>> attrs,
            String address,
            Object... uriVariables

    ) throws HttpStatusCodeException {

        return client.post()
                .uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(attrs != null ? attrs : a -> {})
                .body(body)
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public <T extends DbItem> T put(
            Class<T> responseType,
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    )throws HttpStatusCodeException {

        return client.put().uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public <T extends DbItem> T put(
            Class<T> responseType,
            T body,
            Consumer<Map<String, Object>> attributes,
            String address,
            Object... uriVariables
    ) throws HttpStatusCodeException {

        return client.put()
                .uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .body(body)
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public <T extends DbItem> T put(
            Class<T> responseType,
            String body,
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException {

        return client.put().uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .attributes(attributes != null ? attributes : a -> {})
                .body(body)
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public <T extends DbItem> T put(
            Class<T> responseType,
            Map<String, String> body,
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws   HttpStatusCodeException{
        return client.put().uri(address, uriVariables)
                .attributes(attributes != null ? attributes : a -> {})
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public void delete (
            Consumer<Map<String, Object>> attrs,
            String uri,
            Object... uriVariables

    ) throws HttpStatusCodeException {
        client.delete().uri(uri, uriVariables)
                .attributes(attrs != null ? attrs : a -> {})
                .retrieve().toBodilessEntity();
    }
}
