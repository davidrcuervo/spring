package com.laetienda.lib.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public interface ToolBoxService {
    public String newToken(int length);
    public String encrypt(String strToEncrypt, String secret);
    public String decrypt(String strToDecrypt, String secret);
    public String getEncode64(String username, String password);
    public RestClient getHttpClient();
    public RestClient getHttpClient(String username, String password);
}
