package com.vomlabs.lpcmmx.obfuscation;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility to encrypt string literals in compiled bytecode
 * This is a preprocessor - in production, use a bytecode manipulation library
 */
public class StringEncryptor {

    private static final byte[] KEY = new byte[] {
        0x4C, 0x50, 0x43, 0x5F, 0x4F, 0x42, 0x46, 0x55,
        0x53, 0x43, 0x41, 0x54, 0x49, 0x4F, 0x4E, 0x00
    };

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: StringEncryptor <input-dir> <output-dir>");
            return;
        }

        File inputDir = new File(args[0]);
        File outputDir = new File(args[1]);

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        processDirectory(inputDir, outputDir);
    }

    private static void processDirectory(File input, File output) {
        File[] files = input.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                File newOutput = new File(output, file.getName());
                newOutput.mkdirs();
                processDirectory(file, newOutput);
            } else if (file.getName().endsWith(".class")) {
                // In a real implementation, use ASM or Javassist to modify bytecode
                // This is a placeholder for the concept
                System.out.println("Would encrypt strings in: " + file.getName());
            }
        }
    }

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
}
