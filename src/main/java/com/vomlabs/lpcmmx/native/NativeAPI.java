package com.vomlabs.lpcmmx.native;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Native C++ API bridge using JNI.
 * Loads the native library and provides native method calls.
 */
public class NativeAPI {

    private static boolean loaded = false;

    static {
        if (!loaded) {
            try {
                // Try to load from resources first, then fall back to system path
                File nativeLib = null;
                String os = System.getProperty("os.name").toLowerCase();
                String libName;
                if (os.contains("win")) {
                    libName = "lpc-native.dll";
                } else if (os.contains("mac")) {
                    libName = "liblpc-native.dylib";
                } else {
                    libName = "liblpc-native.so";
                }

                System.loadLibrary("lpc-native");
                loaded = true;
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
            }
        }
    }

    public static boolean isNativeLoaded() {
        return loaded;
    }

    // Native methods - implemented in C++
    private static native String filterSwearWordsNative(String input, String[] words, String replacement);
    private static native boolean isSpamNative(long[] timestamps, long currentTime, long minTime, int maxMessages, int windowSeconds);
    private static native String renderFormatNative(String format, String[] placeholders, String[] values);

    // Public API with fallback to Java
    public static String filterSwearWords(String input, List<String> words, String replacement) {
        if (loaded) {
            try {
                String[] wordsArray = words.toArray(new String[0]);
                return filterSwearWordsNative(input, wordsArray, replacement);
            } catch (Exception e) {
                // Fallback to Java
            }
        }
        return filterSwearWordsJava(input, words, replacement);
    }

    public static boolean isSpam(long[] timestamps, long currentTime, long minTime, int maxMessages, int windowSeconds) {
        if (loaded) {
            try {
                return isSpamNative(timestamps, currentTime, minTime, maxMessages, windowSeconds);
            } catch (Exception e) {
                // Fallback to Java
            }
        }
        return isSpamJava(timestamps, currentTime, minTime, maxMessages, windowSeconds);
    }

    public static String renderFormat(String format, List<String> placeholders, List<String> values) {
        if (loaded) {
            try {
                String[] phArray = placeholders.toArray(new String[0]);
                String[] valArray = values.toArray(new String[0]);
                return renderFormatNative(format, phArray, valArray);
            } catch (Exception e) {
                // Fallback to Java
            }
        }
        return renderFormatJava(format, placeholders, values);
    }

    // Java fallback implementations
    private static String filterSwearWordsJava(String input, List<String> words, String replacement) {
        String result = input.toLowerCase();
        for (String word : words) {
            if (word == null || word.isEmpty()) continue;
            StringBuilder sb = new StringBuilder();
            int lastIndex = 0;
            int index;
            while ((index = result.indexOf(word.toLowerCase(), lastIndex)) != -1) {
                sb.append(result, lastIndex, index);
                boolean isStart = (index == 0 || !Character.isLetter(result.charAt(index - 1)));
                boolean isEnd = (index + word.length() >= result.length() ||
                        !Character.isLetter(result.charAt(index + word.length())));
                if (isStart && isEnd) {
                    sb.append(replacement.repeat(word.length()));
                } else {
                    sb.append(result, index, index + word.length());
                }
                lastIndex = index + word.length();
            }
            sb.append(result.substring(lastIndex));
            result = sb.toString();
        }
        return result;
    }

    private static boolean isSpamJava(long[] timestamps, long currentTime, long minTime, int maxMessages, int windowSeconds) {
        if (timestamps == null) return false;

        java.util.List<Long> valid = new java.util.ArrayList<>();
        for (long ts : timestamps) {
            if ((currentTime - ts) <= (windowSeconds * 1000L)) {
                valid.add(ts);
            }
        }

        if (!valid.isEmpty()) {
            long lastMsgTime = valid.get(valid.size() - 1);
            if ((currentTime - lastMsgTime) < minTime) {
                return true;
            }
        }

        return valid.size() >= maxMessages;
    }

    private static String renderFormatJava(String format, List<String> placeholders, List<String> values) {
        String result = format;
        for (int i = 0; i < placeholders.size() && i < values.size(); i++) {
            result = result.replace(placeholders.get(i), values.get(i));
        }
        return result;
    }
}
