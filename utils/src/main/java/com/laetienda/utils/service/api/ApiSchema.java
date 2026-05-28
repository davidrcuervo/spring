package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbItem;
import com.laetienda.model.schema.ItemTypeA;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface ApiSchema extends ApiRestClient {
    <T extends DbItem> long isItemValid(Class<T> clazz, Long itemId) throws HttpStatusCodeException;
    <T extends DbItem> T create(Class<T> clazz, T item) throws HttpStatusCodeException;
    <T extends DbItem> T create(Class<T> clazz, T item, String token) throws HttpStatusCodeException;
    <T extends DbItem> T find(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException;
    <T extends DbItem> T findByServiceId(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException;
    <T extends DbItem> T findById(Class<T> clazz, Long id) throws HttpStatusCodeException;
    <T extends DbItem> List<T> findAll(Class<T> clazz, Map<String, String> params) throws HttpStatusCodeException;
    <T extends DbItem> List<T> findAllWithToken(Class<T> clazz, Map<String, String> params, String token) throws HttpStatusCodeException;
    <T extends DbItem> List<T> findByQuery(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException;
    <T extends DbItem> List<T> findByQueryByClientRegistrationId(Class<T> clazz, Map<String, String> body) throws HttpStatusCodeException;
    <T extends DbItem> void delete(Class<T> clazz, Map<String, String> body) throws HttpClientErrorException;
    <T extends DbItem> void deleteById(Class<T> clazz, Long id) throws HttpStatusCodeException;
    <T extends DbItem> void deleteById(Class<T> clazz, Long id, String token) throws HttpStatusCodeException;
    <T extends DbItem> T update(Class<T> clazz, DbItem item) throws HttpStatusCodeException;
    <T extends DbItem> String getClazzName(Class<T> clazz);
    <T extends DbItem> List<String> getReaders(Class<T> clazz, Long id, String token) throws HttpStatusCodeException;
    <T extends DbItem> List<String> getEditors(Class<T> clazz, Long id, String token) throws HttpStatusCodeException;
}
