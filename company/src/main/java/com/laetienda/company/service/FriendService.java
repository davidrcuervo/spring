package com.laetienda.company.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Friend;
import jakarta.validation.Valid;

import java.util.List;

public interface FriendService {
    Friend find(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException;
    List<Friend> findAll(String companyId, String userId) throws NotValidCustomException;
    Friend add(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException;
    Friend update(@Valid Friend friend) throws NotValidCustomException;
    void delete(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException;
}
