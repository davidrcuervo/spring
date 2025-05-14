package com.laetienda.webapp_test.service;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import com.laetienda.utils.service.api.GroupApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

public class GroupTestServiceImplementation implements GroupTestService{
    private final static Logger log = LoggerFactory.getLogger(GroupTestServiceImplementation.class);

    @Value("${admuser.username}")
    private String admuser;

    @Autowired
    private GroupApi groupApi;

    private String port;

    @Value("${admuser.password}")
    private String admuserPassword;

    @Override
    public GroupTestService setPort(String port) {
        this.port = port;
        return this;
    }

    @Override
    public GroupTestService setPort(Integer port) {
        setPort(Integer.toString(port));
        return this;
    }

    @Override
    public ResponseEntity<Group> create(Group group) throws HttpClientErrorException {
        return create(group, admuser, admuserPassword);
    }

    @Override
    public ResponseEntity<Group> create(Group group, String loginUsername, String password) throws HttpClientErrorException {
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            findByName(group.getName());
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        ResponseEntity<Group> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .create(group);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        findByName(group.getName());

        return response;
    }

    @Override
    public ResponseEntity<Group> findByName(String gname) throws HttpClientErrorException {
        log.trace("GROUP_TEST::FindByName. $gname: {}, $admuser: {}", gname, admuser);
        return findByName(gname, admuser, admuserPassword);
    }

    @Override
    public ResponseEntity<Group> findByName(String gname, String loginPassword, String password) throws HttpClientErrorException {
        ResponseEntity<Group> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginPassword, password))
                .findByName(gname);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getOwners().size() > 0);
        assertTrue(response.getBody().getMembers().size() > 0);
        return response;
    }

    @Override
    public ResponseEntity<String> delete(String gname, String loginUsername, String password) throws HttpClientErrorException {
        findByName(gname);
        ResponseEntity<String> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                        .delete(gname);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(Boolean.parseBoolean(response.getBody()));

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> {
            findByName(gname);
        });
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        return response;
    }

    @Override
    public ResponseEntity<String> delete(String gname) throws HttpClientErrorException {
        return delete(gname, admuser, admuserPassword);
    }

    @Override
    public ResponseEntity<Group> addMember(String gname, String username, String loginUsername, String password) throws HttpClientErrorException {
        log.trace("GROUP_TEST::addMember. $groupname: {}, $username: {}, $loginUsername: {}", gname, username, loginUsername);

        ResponseEntity<Group> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .addMember(gname, username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMembers().containsKey(username));
        isMember(gname, username, loginUsername, password);

        return response;
    }

    @Override
    public ResponseEntity<Group> removeMember(String gname, String username, String loginUsername, String password) throws HttpClientErrorException {
        log.trace("GROUP_TEST::removeMember. $groupname: {}, $username: {}, $loginUsername: {}", gname, username, loginUsername);

        ResponseEntity<Group> response= ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .removeMember(gname, username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getMembers().containsKey(username));
        isNotMember(gname, username, loginUsername, password);

        return response;
    }

    @Override
    public ResponseEntity<Group> addOwner(String gname, String username, String loginUsername, String password) throws HttpClientErrorException {
        isNotOwner(gname, username, loginUsername, password);

        ResponseEntity<Group> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .addOwner(gname, username);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getOwners().containsKey(username));
        assertTrue(response.getBody().getMembers().containsKey(username));
        isOwner(gname, username, loginUsername, password);
        isMember(gname, username, loginUsername, password);
        return response;
    }

    @Override
    public ResponseEntity<Group> removeOwner(String gname, String username, String loginUsername, String password) throws HttpClientErrorException {
        isOwner(gname, username, loginUsername, password);
        ResponseEntity<Group> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .removeOwner(gname, username);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().getOwners().containsKey(username));

        return response;
    }

    @Override
    public ResponseEntity<String> isOwner(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException {
        ResponseEntity<String> response = checkOwner(groupname, username, loginUsername, password);
        assertTrue(Boolean.parseBoolean(response.getBody()));
        return response;
    }

    @Override
    public ResponseEntity<String> isNotOwner(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException {
        ResponseEntity<String> response = checkOwner(groupname, username, loginUsername, password);
        assertFalse(Boolean.parseBoolean(response.getBody()));
        return response;
    }

    @Override
    public ResponseEntity<Group> update(String groupname, Group group) throws HttpClientErrorException {
        return update(groupname, group, admuser, admuserPassword);
    }

    @Override
    public ResponseEntity<Group> update(String groupname, Group group, String loginUsername, String password) throws HttpClientErrorException {
        ResponseEntity<Group> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .update(groupname, group);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }

    @Override
    public ResponseEntity<GroupList> findAll(String loginUsername, String password) throws HttpClientErrorException {
        ResponseEntity<GroupList> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .findAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        return response;
    }

    @Override
    public ResponseEntity<GroupList> findAll() throws HttpClientErrorException {
        return findAll(admuser, admuserPassword);
    }

    private ResponseEntity<String> checkOwner(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException{
        ResponseEntity<String> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .isOwner(groupname, username);

        assertEquals(HttpStatus.OK,  response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }

    @Override
    public ResponseEntity<String> isMember(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException {
        log.trace("GROUP_TEST::isMember. $groupname: {}, $username: {}", groupname, username);

        ResponseEntity<String> response = checkMember(groupname, username, loginUsername, password);
        assertTrue(Boolean.parseBoolean(response.getBody()));
        return response;
    }

    @Override
    public ResponseEntity<String> isNotMember(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException {
        log.trace("GROUP_TEST::isNotMember. $groupname: {}, $username: {}, $loginUsername: {}", groupname, username, loginUsername);

        ResponseEntity<String> response = checkMember(groupname, username, loginUsername, password);
        assertFalse(Boolean.parseBoolean(response.getBody()));
        return response;
    }

    private ResponseEntity<String> checkMember(String groupname, String username, String loginUsername, String password) throws HttpClientErrorException{
        ResponseEntity<String> response = ((GroupApi)groupApi.setPort(port).setCredentials(loginUsername, password))
                .isMember(groupname, username);

        assertEquals(HttpStatus.OK,  response.getStatusCode());
        assertNotNull(response.getBody());
        return response;
    }
}
