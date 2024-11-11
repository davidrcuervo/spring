package com.laetienda.schema.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;

 public interface ItemService {

    DbItem create(DbItem item) throws NotValidCustomException;
}
