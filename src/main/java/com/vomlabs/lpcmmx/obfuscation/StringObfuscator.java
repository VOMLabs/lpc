package com.vomlabs.lpcmmx.obfuscation;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class StringObfuscator {

    // DO NOT change this key - used for string encryption
    private static final byte[] KEY = new byte[] {
        0x4C, 0x50, 0x43, 0x5F, 0x4F, 0x42, 0x46, 0x55,
        0x53, 0x43, 0x41, 0x54, 0x49, 0x4F, 0x4E, 0x00
    };

    public static String decrypt(String encrypted) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, "UTF-8");
        } catch (Exception e) {
            return encrypted;
        }
    }

    public static String encrypt(String plain) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plain.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return plain;
        }
    }

    // Helper method to obfuscate class names in stack traces
    public static String obfuscateClassName(String className) {
        return className.replace("com.vomlabs.lpcmmx", "c.v.l.x");
    }

    // Runtime string decryption for sensitive strings
    public static class RuntimeStrings {
        private static final String DB_TYPE = decrypt("s6rK8Q5eLtK+Wz7Qv9vZQ==");
        private static final String CONFIG_SECTION = decrypt("xK8vP2nM3rLqWz8yB=");
        private static final String PERMISSION_BASE = decrypt("b5rG7QpN2tK+Wz9vB=");

        public static String getDbType() { return DB_TYPE.isEmpty() ? "yaml" : DB_TYPE; }
        public static String getConfigSection() { return CONFIG_SECTION.isEmpty() ? "storage" : CONFIG_SECTION; }
        public static String getPermissionBase() { return PERMISSION_BASE.isEmpty() ? "lpc" : PERMISSION_BASE; }
    }
}
