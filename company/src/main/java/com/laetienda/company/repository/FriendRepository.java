package com.laetienda.company.repository;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Company;
import com.laetienda.model.company.Friend;
import com.laetienda.model.company.Member;

import java.util.List;

public interface FriendRepository {
    List<Friend> find(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException;
    List<Friend> findByNoJwt(String companyId, String memberUserId, String friendUserId) throws NotValidCustomException;
    List<Friend> findAll(Long cid, String uid) throws NotValidCustomException;
    Friend create(Friend friend) throws NotValidCustomException;
    Friend update(Friend friend) throws NotValidCustomException;
    void delete(Friend friend) throws NotValidCustomException;
}
