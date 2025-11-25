package com.laetienda.company.lib;

import com.laetienda.lib.exception.NotValidCustomException;
import com.laetienda.model.company.Friend;
import com.laetienda.model.messager.EmailMessage;

public interface EmailService {
    void sendFriendRequest(Friend friend) throws NotValidCustomException;
}
