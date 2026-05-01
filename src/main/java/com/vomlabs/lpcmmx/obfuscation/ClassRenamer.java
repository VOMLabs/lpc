package com.vomlabs.lpcmmx.obfuscation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Runtime class renaming utility
 * Renames classes to meaningless names at runtime
 */
public class ClassRenamer {

    private static final Map<String, String> nameMapping = new HashMap<>();
    private static final Random random = new Random(System.currentTimeMillis());
    private static boolean initialized = false;

    // Obfuscated class name prefixes
    private static final String[] PREFIXES = {
        "a", "b", "c", "d", "e", "f", "g", "h"
    };

    private static final String[] SUFFIXES = {
        "x", "y", "z", "w", "m", "n", "p", "q"
    };

    public static void initialize() {
        if (initialized) return;

        // Generate random mappings for sensitive classes
        mapClass("Main");
        mapClass("ChatFilter");
        mapClass("MuteManager");
        mapClass("MessageManager");
        mapClass("EncryptionManager");
        mapClass("DiscordWebhook");
        mapClass("ChatLogger");
        mapClass("LPCChatRenderer");

        initialized = true;
    }

    private static void mapClass(String className) {
        String obfuscated = PREFIXES[random.nextInt(PREFIXES.length)] +
                          SUFFIXES[random.nextInt(SUFFIXES.length)] +
                          Integer.toHexString(random.nextInt(256));
        nameMapping.put(className, obfuscated);
    }

    public static String getObfuscatedName(String originalName) {
        return nameMapping.getOrDefault(originalName, originalName);
    }

    public static String obfuscateStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String obfClass = getObfuscatedName(className);
            sb.append("\tat ").append(obfClass)
              .append(".").append(element.getMethodName())
              .append("(").append(element.getFileName())
              .append(":").append(element.getLineNumber()).append(")\n");
        }
        return sb.toString();
    }
}
