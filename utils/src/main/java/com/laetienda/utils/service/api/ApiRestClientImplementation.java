package com.laetienda.utils.service.api;

import com.laetienda.lib.service.ToolBoxService;
import com.laetienda.model.schema.DbItem;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ApiRestClientImplementation implements ApiRestClient {

    private final RestClient client;
    private final ToolBoxService tb;

    public ApiRestClientImplementation(
            RestClient restClient,
            ToolBoxService toolBoxService
    ) {
        this.client = restClient;
        this.tb = toolBoxService;
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
            Map<String, String> params,
            Object... uriVariables

    ) throws HttpStatusCodeException {

        ParameterizedTypeReference<List<T>> typeRef = ParameterizedTypeReference.forType(
                ResolvableType.forClassWithGenerics(List.class, responseType).getType()
        );

        String uri = tb.setAddressParams(params, address, uriVariables);

        return client.get()
                .uri(uri)
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
    public <T extends DbItem> T post(
        Class<T> responseType,
        Map<String, String> params,
        Consumer<Map<String, Object>> attrs,
        String address,
        Object... uriVariables
    ) throws HttpStatusCodeException{

        return client.post()
                .uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(attrs != null ? attrs : a -> {})
                .body(params)
                .retrieve()
                .toEntity(responseType)
                .getBody();
    }

    @Override
    public String post(
            String jsonBody,
            Consumer<Map<String, Object>> attrs,
            String address,
            Object... uriVariables
    ) throws HttpStatusCodeException {
        return client.post()
                .uri(address, uriVariables)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .attributes(attrs != null ? attrs : a -> {})
                .body(jsonBody)
                .retrieve()
                .toEntity(String.class)
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
    public void put(
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException {
        client.put().uri(address, uriVariables)
                .attributes(attributes != null ? attributes : a -> {})
                .retrieve()
                .toBodilessEntity();
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
