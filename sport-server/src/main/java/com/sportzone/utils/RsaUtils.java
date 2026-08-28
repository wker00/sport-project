package com.sportzone.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaUtils {

    private PrivateKey privateKey;

    @Value("${rsa.privateKey}")
    private String privateKeyBase64;

    @Value("${rsa.publicKey}")
    private String publicKeyBase64;

    @jakarta.annotation.PostConstruct
    public void init() throws Exception {
        String cleanKey = privateKeyBase64.replaceAll("\\s", "");
        byte[] keyBytes = Base64.getDecoder().decode(cleanKey);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        this.privateKey = keyFactory.generatePrivate(spec);
    }

    public String decrypt(String encryptedBase64) {
        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedBase64);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("密码解密失败", e);
        }
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }
}
