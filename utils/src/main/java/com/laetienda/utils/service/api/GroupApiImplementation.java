package com.laetienda.utils.service.api;

import com.laetienda.model.user.Group;
import com.laetienda.model.user.GroupList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

public class GroupApiImplementation extends ApiClientImplementation implements GroupApi {
    private static final Logger log = LoggerFactory.getLogger(GroupApiImplementation.class);

    @Value("${api.usuario.port}")
    private String apiPort;

    @Value("${api.group.findAll}")
    private String findAll;

    @Value("${api.group.findByName}")
    private String findByName;

    @Value("${api.group.create}")
    private String create;

    @Value("${api.group.addMember}")
    private String addMember;

    @Value("${api.group.removeMember}")
    private String removeMember;

    @Value("${api.group.isMember}")
    private String isMemeber;

    @Value("${api.group.isOwner}")
    private String isOwner;

    @Value("${api.group.update}")
    private String update;

    @Value("${api.group.addOwner}")
    private String addOwner;

    @Value("${api.group.removeOwner}")
    private String removeOwner;

    @Value("${api.group.delete}")
    private String delete;

    @Value("${api.group}/${api.group.findAllByMember}")
    private String findAllByMember;

    @Override
    public ResponseEntity<Group> findByName(String gname) throws HttpClientErrorException {
        log.trace("Requesting find group by name. $uri: {}", findByName);
        return getRestClient().get().uri(findByName, getPort(), gname)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<Group> create(Group group) throws HttpClientErrorException {
        log.trace("GROUP_API::Create. $uri: {}, $port: {}", create, getPort());
        return getRestClient().post().uri(create, getPort())
                .contentType(MediaType.APPLICATION_JSON)
                .body(group)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<String> delete(String gname) throws HttpClientErrorException {
        return getRestClient().delete().uri(delete, getPort(), gname)
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<Group> addMember(String groupname, String username) throws HttpClientErrorException {
        return getRestClient().put().uri(addMember, getPort(), username, groupname)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<Group> removeMember(String groupname, String username) throws HttpClientErrorException {
        return getRestClient().delete().uri(removeMember, getPort(), groupname, username)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<String> isMember(String groupname, String username) throws HttpClientErrorException {
        return getRestClient().get()
                .uri(isMemeber, getPort(), username, groupname)
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<String> isOwner(String groupname, String username) throws HttpClientErrorException {
        return getRestClient().get()
                .uri(isOwner, getPort(), groupname, username)
                .retrieve().toEntity(String.class);
    }

    @Override
    public ResponseEntity<Group> addOwner(String groupname, String username) throws HttpClientErrorException {
        return getRestClient().put()
                .uri(addOwner, getPort(), groupname, username)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<Group> removeOwner(String groupname, String username) throws HttpClientErrorException {
        return getRestClient().delete()
                .uri(removeOwner, getPort(), groupname, username)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<Group> update(String groupname, Group group) throws HttpClientErrorException {
        return getRestClient().put()
                .uri(update, getPort(), groupname)
                .body(group)
                .retrieve().toEntity(Group.class);
    }

    @Override
    public ResponseEntity<GroupList> findAll() throws HttpClientErrorException {
        return getRestClient().get()
                .uri(findAll, getPort())
                .retrieve().toEntity(GroupList.class);
    }

    @Override
    public ResponseEntity<GroupList> findAllByMember(String username) throws HttpClientErrorException {
        log.debug("GROUP_API::findAllByMember. $path: {}, $username: {}", findAllByMember, username);
        return getRestClient().get()
                .uri(findAllByMember, getPort(), username)
                .retrieve().toEntity(GroupList.class);
    }

    public String getPort(){
        if(super.getPort() == null){
            super.setPort(apiPort);
        }
        return super.getPort();
    }
}