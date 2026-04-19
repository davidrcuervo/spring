package com.laetienda.utils.service.api;

import com.laetienda.model.schema.DbGroup;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Map;

public interface ApiSchemaGroup extends ApiRestClient {
    List<DbGroup> findAll() throws HttpStatusCodeException;
    List<DbGroup> findAll(String token) throws HttpStatusCodeException;
    List<DbGroup> findAllByService(String clientRegistrationId) throws HttpStatusCodeException;
    DbGroup find(Long groupId) throws HttpStatusCodeException;
    DbGroup find(Long groupId, String token) throws HttpStatusCodeException;
    DbGroup findByName(String name) throws HttpStatusCodeException;
    DbGroup findByName(String name, String token) throws HttpStatusCodeException;
    DbGroup update(Long groupId, Map<String, String> body) throws HttpStatusCodeException;
    DbGroup update(Long groupId, Map<String, String> body, String token) throws HttpStatusCodeException;
    void delete(Long groupId) throws HttpStatusCodeException;
    void delete(Long groupId, String token) throws HttpStatusCodeException;
    List<DbGroup> getOrphans() throws HttpStatusCodeException;
    List<DbGroup> getOrphans(String token) throws HttpStatusCodeException;
    DbGroup addMember(Long groupId, String userId) throws HttpStatusCodeException;
    DbGroup addMember(Long groupId, String userId, String token) throws HttpStatusCodeException;
    DbGroup addMemberByService(Long groupId, String userId) throws HttpStatusCodeException;
    DbGroup removeMember(Long groupId, String userId) throws HttpStatusCodeException;
    DbGroup removeMember(Long groupId, String userId, String token) throws HttpStatusCodeException;
}
