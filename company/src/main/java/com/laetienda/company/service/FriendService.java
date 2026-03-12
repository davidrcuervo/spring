package com.laetienda.company.service;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Friend;
import jakarta.validation.Valid;

import java.util.List;

public interface FriendService {
    Friend find(String companyId, String userId) throws NotValidCustomException;
    List<Friend> findAll(String companyId, String userId) throws NotValidCustomException;
    Friend add(String companyId, String userId) throws NotValidCustomException;
    void delete(String companyId, String userId) throws NotValidCustomException;
    Friend accept(String companyId, String userId) throws NotValidCustomException;
    Friend block(String companyId, String userId) throws NotValidCustomException;
    Friend unblock(String companyId, String userId) throws NotValidCustomException;
}
