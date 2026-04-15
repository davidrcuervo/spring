package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbGroup;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface ApiSchemaGroup extends ApiRestClient {
    List<DbGroup> findAll() throws HttpStatusCodeException;
    DbGroup find(Long groupId) throws HttpStatusCodeException;
    DbGroup findByName(String name) throws HttpStatusCodeException;
    DbGroup update(Long groupId, Map<String, String> body) throws HttpStatusCodeException;
    void delete(Long groupId) throws HttpStatusCodeException;
    List<DbGroup> getOrphans() throws HttpStatusCodeException;
    DbGroup addMember(Long groupId, String userId) throws HttpStatusCodeException;
    DbGroup removeMember(Long groupId, String userId) throws HttpStatusCodeException;
}
