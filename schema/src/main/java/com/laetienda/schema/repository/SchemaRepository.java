package com.laetienda.schema.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Map;
import java.util.List;

public interface SchemaRepository {
    <T> T create(Class<T> clazz, DbItem item) throws HttpServerErrorException;
    <T> T update(Class<T> clazz, DbItem item) throws NotValidCustomException;
    <T> T find(Class<T> clazz, Map<String, String> body);

    List<? extends DbItem> findAll(
            Class<? extends DbItem> clazz,
            Map<String, String> params,
            String currentUserId
    ) throws HttpServerErrorException;

    <T> List<T> findByQuery(Class<T> clazz, Map<String, String> body) throws NotValidCustomException;
    <T> void delete(Class<T> clazz, T item) throws HttpServerErrorException;
    <T> T findById(Long id, Class<T> clazz) throws HttpStatusCodeException;
    boolean deleteUserById(String userId);

}
