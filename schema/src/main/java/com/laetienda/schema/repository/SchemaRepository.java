package com.laetienda.schema.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;

import java.util.Map;

public interface SchemaRepository {
    <T> T create(Class<T> clazz, DbItem item) throws NotValidCustomException;
    <T> T update(Class<T> clazz, DbItem item) throws NotValidCustomException;
    <T> T find(Class<T> clazz, Map<String, String> body);
    <T> void delete(Class<T> clazz, T item);
    <T> T findById(Long id, Class<T> clazz) throws NotValidCustomException;
}
