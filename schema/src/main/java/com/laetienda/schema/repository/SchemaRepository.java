package com.laetienda.schema.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;

import java.util.Map;

public interface SchemaRepository {
    public DbItem create(Class<?> clazz, DbItem item) throws NotValidCustomException;
    <T> T find(Class<T> clazz, Map<String, String> body);
    <T> void delete(Class<T> clazz, T item);
}
