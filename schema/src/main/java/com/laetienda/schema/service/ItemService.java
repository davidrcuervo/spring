package com.laetienda.schema.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.schema.DbItem;

import java.util.Map;

public interface ItemService {

    <T> T create(Class<T> clazz, String data) throws NotValidCustomException;
    <T> T find(Class<T> clazz, Map<String, String> body) throws NotValidCustomException;
    <T> T findById(Class<T> clazz, Long id) throws NotValidCustomException;
    <T> void delete(Class<T> clazz, Map<String, String> body) throws NotValidCustomException;
    <T> void deleteById(Class<T> clazz, Long id) throws NotValidCustomException;
    <T> T update(Class<T> clazz, String data) throws NotValidCustomException;
}
