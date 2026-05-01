package com.vomlabs.lpcmmx.native;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class NativeAPI {

    private static boolean loaded = false;

    static {
        if (!loaded) {
            try {
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

    private static native String filterSwearWordsNative(String input, String[] words, String replacement);
    private static native boolean isSpamNative(long[] timestamps, long currentTime, long minTime, int maxMessages, int windowSeconds);
    private static native String renderFormatNative(String format, String[] placeholders, String[] values);
    private static native String convertLegacyToMiniMessageNative(String input);
    private static native String formatUUIDNative(String uuid);
    private static native int hashStringNative(String input);
    private static native String[] parseGradientColorsNative(String gradientTag);
    private static native boolean containsFilteredContentNative(String message, boolean filterItemNames);
    private static native String toLowerCaseNative(String input);
    private static native boolean arrayContainsNative(String[] array, String target);
    private static native int countPlaceholdersNative(String format, String placeholderPrefix);
    private static native String fastConcatNative(String[] strings);

    public static String filterSwearWords(String input, List<String> words, String replacement) {
        if (loaded) {
            try {
                String[] wordsArray = words.toArray(new String[0]);
                return filterSwearWordsNative(input, wordsArray, replacement);
            } catch (Exception e) {
                // Fallback
            }
        }
        return filterSwearWordsJava(input, words, replacement);
    }

    public static boolean isSpam(long[] timestamps, long currentTime, long minTime, int maxMessages, int windowSeconds) {
        if (loaded) {
            try {
                return isSpamNative(timestamps, currentTime, minTime, maxMessages, windowSeconds);
            } catch (Exception e) {
                // Fallback
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
                // Fallback
            }
        }
        return renderFormatJava(format, placeholders, values);
    }

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

    public static String convertLegacyToMiniMessage(String input) {
        if (loaded) {
            try {
                return convertLegacyToMiniMessageNative(input);
            } catch (Exception e) {
                // Fallback
            }
        }
        return convertLegacyToMiniMessageJava(input);
    }

    public static String formatUUID(String uuid) {
        if (loaded) {
            try {
                return formatUUIDNative(uuid);
            } catch (Exception e) {
                // Fallback
            }
        }
        return formatUUIDJava(uuid);
    }

    public static int hashString(String input) {
        if (loaded) {
            try {
                return hashStringNative(input);
            } catch (Exception e) {
                // Fallback
            }
        }
        return hashStringJava(input);
    }

    public static String[] parseGradientColors(String gradientTag) {
        if (loaded) {
            try {
                return parseGradientColorsNative(gradientTag);
            } catch (Exception e) {
                // Fallback
            }
        }
        return parseGradientColorsJava(gradientTag);
    }

    public static boolean containsFilteredContent(String message, boolean filterItemNames) {
        if (loaded) {
            try {
                return containsFilteredContentNative(message, filterItemNames);
            } catch (Exception e) {
                // Fallback
            }
        }
        return containsFilteredContentJava(message, filterItemNames);
    }

    private static String convertLegacyToMiniMessageJava(String input) {
        String result = input;
        result = result.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&r", "<reset>")
                .replace("&l", "<bold>")
                .replace("&o", "<italic>")
                .replace("&n", "<underlined>")
                .replace("&m", "<strikethrough>")
                .replace("&k", "<obfuscated>");
        return result;
    }

    private static String formatUUIDJava(String uuid) {
        if (uuid == null) return null;
        String cleaned = uuid.replace("-", "").toLowerCase();
        if (cleaned.length() != 32) return null;
        for (char c : cleaned.toCharArray()) {
            if (!Character.isDigit(c) && (c < 'a' || c > 'f')) return null;
        }
        return cleaned.substring(0, 8) + "-" +
               cleaned.substring(8, 12) + "-" +
               cleaned.substring(12, 16) + "-" +
               cleaned.substring(16, 20) + "-" +
               cleaned.substring(20, 32);
    }

    private static int hashStringJava(String input) {
        long hash = 5381;
        for (byte b : input.getBytes()) {
            hash = ((hash << 5) + hash) + b;
        }
        return (int)(hash & 0xFFFFFFFFL);
    }

    private static String[] parseGradientColorsJava(String gradientTag) {
        if (gradientTag == null) return new String[0];
        int start = gradientTag.indexOf("<gradient:");
        if (start == -1) return new String[0];
        int end = gradientTag.indexOf(">", start);
        if (end == -1) return new String[0];
        String content = gradientTag.substring(start + 10, end);
        return content.split(":");
    }

    private static boolean containsFilteredContentJava(String message, boolean filterItemNames) {
        String lower = message.toLowerCase();
        if (filterItemNames && lower.contains("[item]")) return true;
        if (lower.contains("http://") || lower.contains("https://")) return true;
        // Simple IP check
        String[] parts = lower.split("\\s+");
        for (String part : parts) {
            if (part.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) return true;
        }
        return false;
    }

    public static String toLowerCase(String input) {
        if (loaded) {
            try {
                return toLowerCaseNative(input);
            } catch (Exception e) {
                // Fallback
            }
        }
        return input.toLowerCase();
    }

    public static boolean arrayContains(String[] array, String target) {
        if (loaded) {
            try {
                return arrayContainsNative(array, target);
            } catch (Exception e) {
                // Fallback
            }
        }
        for (String s : array) {
            if (s.equals(target)) return true;
        }
        return false;
    }

    public static int countPlaceholders(String format, String placeholderPrefix) {
        if (loaded) {
            try {
                return countPlaceholdersNative(format, placeholderPrefix);
            } catch (Exception e) {
                // Fallback
            }
        }
        int count = 0;
        int pos = 0;
        while ((pos = format.indexOf(placeholderPrefix, pos)) != -1) {
            count++;
            pos += placeholderPrefix.length();
        }
        return count;
    }

    public static String fastConcat(String[] strings) {
        if (loaded) {
            try {
                return fastConcatNative(strings);
            } catch (Exception e) {
                // Fallback
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            sb.append(s);
        }
        return sb.toString();
    }
}
