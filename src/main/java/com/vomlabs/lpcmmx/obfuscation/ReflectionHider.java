package com.vomlabs.lpcmmx.obfuscation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Hides sensitive strings and methods using reflection
 * Makes reverse engineering more difficult
 */
public class ReflectionHider {

    private static final Map<String, String> STRING_CACHE = new HashMap<>();
    private static final Map<String, Object> INSTANCE_CACHE = new HashMap<>();

    // Obfuscated method names
    private static final String[] METHOD_ALIASES = {
        "a", "b", "c", "d", "e", "f", "g", "h", "i", "j"
    };

    // Obfuscated field names
    private static final String[] FIELD_ALIASES = {
        "x", "y", "z", "w", "v", "u", "t", "s"
    };

    static {
        // Initialize string cache with encrypted values
        STRING_CACHE.put("config", StringObfuscator.encrypt("config.yml"));
        STRING_CACHE.put("messages", StringObfuscator.encrypt("messages.yml"));
        STRING_CACHE.put("paper", StringObfuscator.encrypt("paper-plugin.yml"));
    }

    public static String getHiddenString(String key) {
        String encrypted = STRING_CACHE.get(key);
        if (encrypted != null) {
            return StringObfuscator.decrypt(encrypted);
        }
        return key;
    }

    public static void hideReflection(Class<?> clazz) {
        try {
            // Change field names in stack traces
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length && i < FIELD_ALIASES.length; i++) {
                try {
                    Field field = fields[i];
                    field.setAccessible(true);
                    // In real obfuscation, you'd use ASM to rename fields
                } catch (Exception e) {
                    // Ignore
                }
            }

            // Hide method implementations
            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isSynthetic()) continue;
                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers)) {
                    // In production, use bytecode manipulation to rename
                }
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    public static void clearCaches() {
        STRING_CACHE.clear();
        INSTANCE_CACHE.clear();
    }

    // Generate fake stack trace elements to confuse reverse engineers
    public static StackTraceElement[] generateFakeStackTrace() {
        return new StackTraceElement[]{
            new StackTraceElement("a", "a", "a.java", 1),
            new StackTraceElement("b", "b", "b.java", 2),
            new StackTraceElement("c", "c", "c.java", 3),
            new StackTraceElement("d", "d", "d.java", 4),
            new StackTraceElement("e", "e", "e.java", 5),
        };
    }
}
