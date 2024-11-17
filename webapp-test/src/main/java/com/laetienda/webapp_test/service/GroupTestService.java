package com.laetienda.webapp_test.service;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public interface GroupTestService {
    public GroupTestService setPort(String port);
    public GroupTestService setPort(Integer port);
    public ResponseEntity<Group> create(Group group) throws HttpClientErrorException;
    public ResponseEntity<Group> create(Group group, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<Group> findByName(String gname) throws HttpClientErrorException;
    public ResponseEntity<Group> findByName(String gname, String loginPassword, String password) throws HttpClientErrorException;
    public ResponseEntity<String> delete(String gname, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<String> delete(String gname) throws HttpClientErrorException;
    public ResponseEntity<String> isMember(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<String> isNotMember(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<Group> addMember(String gname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<Group> removeMember(String gname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<Group> addOwner(String gname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<Group> removeOwner(String gname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<String> isOwner(String gname, String username, String loginUsername, String password) throws HttpClientErrorException;
    public ResponseEntity<String> isNotOwner(String gname, String username, String loginUsername, String password) throws HttpClientErrorException;
    ResponseEntity<Group> update (String groupname, Group group) throws HttpClientErrorException;
    ResponseEntity<Group> update (String groupname, Group group, String loginUsername, String password) throws HttpClientErrorException;
    ResponseEntity<GroupList> findAll(String loginUsername, String password) throws HttpClientErrorException;
    ResponseEntity<GroupList> findAll() throws HttpClientErrorException;
}
