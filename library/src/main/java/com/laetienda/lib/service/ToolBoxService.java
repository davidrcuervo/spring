package com.laetienda.lib.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public interface ToolBoxService {
    public String newToken(int length);
    public String encrypt(String strToEncrypt, String secret);
    public String decrypt(String strToDecrypt, String secret);
    public String getEncode64(String username, String password);
    public RestClient getHttpClient();
    public RestClient getHttpClient(String username, String password);
    String getCurrentUserId();
    public String getCurrentUsername();
    boolean hasAuthority(String authority);
    void isServiceIfNotThrowException() throws HttpServerErrorException;
    String setAddressParams(Map<String, String> params, String address, Object... uriComponents);
}
