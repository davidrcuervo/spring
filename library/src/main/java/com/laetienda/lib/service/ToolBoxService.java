package com.laetienda.lib.service;

import org.springframework.stereotype.Service;

@Service
public interface ToolBoxService {
    public String newToken(int length);
    public String encrypt(String strToEncrypt, String secret);
    public String decrypt(String strToDecrypt, String secret);
}
