package com.laetienda.lib.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class ToolBoxServiceImpl implements ToolBoxService {
    final private static Logger log = LoggerFactory.getLogger(ToolBoxServiceImpl.class);
    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe
    @Override
    public String newToken(int length) {
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    @Override
    public String encrypt(String strToEncrypt, String secret) {

        SecretKeySpec secretKeySpec = setKey(secret);
        String result = null;

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            result = Base64.getUrlEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String decrypt(String strToDecrypt, String secret) {
        String result = null;

        SecretKeySpec secretKeySpec = setKey(secret);
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            result = new String(cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private SecretKeySpec setKey(String secret){
        byte[] key = null;
        MessageDigest sha = null;
        SecretKeySpec secretKeySpec;

        try {
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(secret.getBytes(StandardCharsets.UTF_8));
            key = Arrays.copyOf(key, 16);
            secretKeySpec = new SecretKeySpec(key, "AES");

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return secretKeySpec;
    }

    public static void main(String args[]){
        ToolBoxServiceImpl tb = new ToolBoxServiceImpl();
        String secret = System.getProperty("jasypt.encryptor.password");
        System.out.println(String.format("Secret has: %s", secret));
        String encrypted = tb.encrypt("oY33NQPEdrOIrhxR", secret);
        System.out.println(String.format("Encrypted: %s", encrypted));
        String decrypted = tb.decrypt(encrypted, secret);
        System.out.println(String.format("Decrypt(%s): %s", encrypted, decrypted));
    }
}
