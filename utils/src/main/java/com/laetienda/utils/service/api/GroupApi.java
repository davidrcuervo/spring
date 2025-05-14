package com.laetienda.utils.service.api;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public interface GroupApi extends ApiClient {
    ResponseEntity<Group> findByName(String gname) throws HttpClientErrorException;
    ResponseEntity<Group> create(Group group) throws HttpClientErrorException;
    public ResponseEntity<String> delete(String gname) throws HttpClientErrorException;
    public ResponseEntity<Group> addMember(String groupname, String username) throws HttpClientErrorException;
    public ResponseEntity<Group> removeMember(String groupname, String username) throws HttpClientErrorException;
    public ResponseEntity<String> isMember (String groupname, String username) throws HttpClientErrorException;
    public ResponseEntity<String> isOwner (String groupname, String username) throws HttpClientErrorException;
    public ResponseEntity<Group> addOwner(String groupname, String username) throws HttpClientErrorException;
    ResponseEntity<Group> removeOwner(String groupname, String username) throws HttpClientErrorException;
    ResponseEntity<Group> update(String groupname, Group group) throws HttpClientErrorException;
    ResponseEntity<GroupList> findAll() throws HttpClientErrorException;
    ResponseEntity<GroupList> findAllByMember(String username) throws HttpClientErrorException;
}
