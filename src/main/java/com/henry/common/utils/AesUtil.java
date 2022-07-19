package com.henry.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
public class AesUtil {
    private static final String KEY_ALGORITHM = "AES";

    private static final String ALGORITHM = "SHA1PRNG";

    private static final int AES_KEY_LEN = 128;

    public static String encrypt(String src, String key) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM);
            secureRandom.setSeed(key.getBytes());
            keyGenerator.init(AES_KEY_LEN, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secKey = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            byte[] byteContent = src.getBytes(StandardCharsets.UTF_8);
            cipher.init(Cipher.ENCRYPT_MODE, secKey);
            byte[] result = cipher.doFinal(byteContent);
            return new String(Base64.getEncoder().encode(result));
        } catch (Exception ex) {
            log.error("encrypt error", ex);
        }
        return null;
    }

    public static String decrypt(String src, String key) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
            SecureRandom secureRandom = SecureRandom.getInstance(ALGORITHM);
            secureRandom.setSeed(key.getBytes());
            keyGenerator.init(AES_KEY_LEN, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secKey = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secKey);
            byte[] decryptContent = src.getBytes();
            byte[] result = cipher.doFinal(Base64.getDecoder().decode(decryptContent));
            return new String(result);
        } catch (Exception ex) {
            log.error("decrypt error", ex);
        }
        return null;
    }

}
