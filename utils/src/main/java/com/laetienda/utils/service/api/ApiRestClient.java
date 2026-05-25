package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface ApiRestClient {

    <T extends DbItem> T get(
            Class<T> responseType,
            Consumer<Map<String, Object>> attributes,
            String address,
            Object... uriVariables
    ) throws HttpStatusCodeException;

    String get(
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException;

    <T extends DbItem> T post(
            Class<T> responseType,
            T body,
            Consumer<Map<String, Object>> attrs,
            String uri,
            Object... uriVariables
    ) throws HttpStatusCodeException;

    <T extends DbItem> T post(
            Class<T> responseType,
            Map<String, String> params,
            Consumer<Map<String, Object>> attrs,
            String uri,
            Object... uriVariables
    ) throws HttpStatusCodeException;

    String post(
            String jsonBody,
            Consumer<Map<String, Object>> attrs,
            String address,
            Object... uriVariables
    ) throws HttpStatusCodeException;

    <T extends DbItem> T put(
            Class<T> responseType,
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    )throws HttpStatusCodeException;

    <T extends DbItem> T put(
            Class<T> responseType,
            T body,
            Consumer<Map<String, Object>> attributes,
            String address,
            Object... uriVariables
    ) throws HttpStatusCodeException;

    <T extends DbItem> T put(
            Class<T> responseType,
            String body,
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException;

    <T extends DbItem> T put(
            Class<T> responseType,
            Map<String, String> body,
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws   HttpStatusCodeException;

    void put(
            Consumer<Map<String, Object>> attributes,
            String address, Object... uriVariables
    ) throws HttpStatusCodeException;

    void delete(
            Consumer<Map<String, Object>> attrs,
            String uri,
            Object... uriVariables
    ) throws HttpStatusCodeException;

    <T extends DbItem> List<T> getList(
            Class<T> responseType,
            Consumer<Map<String, Object>> attributes,
            String address,
            Object... uriVariables
    ) throws HttpStatusCodeException;
}
