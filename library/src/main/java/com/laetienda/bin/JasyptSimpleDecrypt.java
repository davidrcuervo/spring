package com.laetienda.bin;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

public class JasyptSimpleDecrypt {

    public static void main(String[] args){
        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword("6xtehPTxAL5Rj6sH3WrB");
        jasypt.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        String result = jasypt.decrypt("ENC(86yPreBLRH2S66fW497434v1DRY+ma21/7kW+QwM0VQbTZHY5jyKRmDjp3e4rumY)");
        System.out.println(result);
    }
}
