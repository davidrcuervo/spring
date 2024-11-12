package com.laetienda.schema.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;

public interface SchemaRepository {
    public DbItem create(Class<?> clazz, DbItem item) throws NotValidCustomException;
}
