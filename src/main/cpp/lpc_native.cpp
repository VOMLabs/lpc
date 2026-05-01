#include "lpc_native.h"
#include <jni.h>
#include <string>
#include <vector>
#include <cctype>
#include <algorithm>
#include <chrono>
#include <sstream>
#include <iomanip>

using namespace std;

// Legacy color codes to MiniMessage mapping
const map<string, string> legacyToMini = {
    {"&0", "<black>"}, {"&1", "<dark_blue>"}, {"&2", "<dark_green>"},
    {"&3", "<dark_aqua>"}, {"&4", "<dark_red>"}, {"&5", "<dark_purple>"},
    {"&6", "<gold>"}, {"&7", "<gray>"}, {"&8", "<dark_gray>"},
    {"&9", "<blue>"}, {"&a", "<green>"}, {"&b", "<aqua>"},
    {"&c", "<red>"}, {"&d", "<light_purple>"}, {"&e", "<yellow>"},
    {"&f", "<white>"}, {"&r", "<reset>"}, {"&l", "<bold>"},
    {"&o", "<italic>"}, {"&n", "<underlined>"}, {"&m", "<strikethrough>"},
    {"&k", "<obfuscated>"}
};

// Helper: convert jstring to std::string
string jstringToString(JNIEnv* env, jstring jStr) {
    if (jStr == nullptr) return "";
    const char* chars = env->GetStringUTFChars(jStr, nullptr);
    string ret(chars);
    env->ReleaseStringUTFChars(jStr, chars);
    return ret;
}

// Helper: convert std::string to jstring
jstring stringToJstring(JNIEnv* env, const string& str) {
    return env->NewStringUTF(str.c_str());
}

// Helper: convert jobjectArray of strings to vector<string>
vector<string> jobjectArrayToStringVector(JNIEnv* env, jobjectArray arr) {
    vector<string> result;
    if (arr == nullptr) return result;
    jsize len = env->GetArrayLength(arr);
    for (jsize i = 0; i < len; i++) {
        jstring elem = (jstring)env->GetObjectArrayElement(arr, i);
        if (elem != nullptr) {
            result.push_back(jstringToString(env, elem));
        }
    }
    return result;
}

extern "C" {

    // Filter swear words - C++ implementation
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_filterSwearWordsNative(
            JNIEnv* env, jclass clazz, jstring input, jobjectArray words, jstring replacement) {

        string message = jstringToString(env, input);
        vector<string> swearWords = jobjectArrayToStringVector(env, words);
        string repl = jstringToString(env, replacement);

        string result = message;
        for (const string& word : swearWords) {
            if (word.empty()) continue;

            string wordLower = word;
            transform(wordLower.begin(), wordLower.end(), wordLower.begin(), ::tolower);

            size_t pos = 0;
            while ((pos = result.find(wordLower, pos)) != string::npos) {
                // Check word boundaries
                bool isStart = (pos == 0 || !isalpha(result[pos - 1]));
                bool isEnd = (pos + wordLower.length() >= result.length() ||
                              !isalpha(result[pos + wordLower.length()]));

                if (isStart && isEnd) {
                    result.replace(pos, wordLower.length(), repl);
                    pos += repl.length();
                } else {
                    pos += wordLower.length();
                }
            }
        }

        return stringToJstring(env, result);
    }

    // Spam detection - C++ implementation
    JNIEXPORT jboolean JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_isSpamNative(
            JNIEnv* env, jclass clazz, jlongArray timestamps, jlong currentTime,
            jlong minTime, jint maxMessages, jint windowSeconds) {

        if (timestamps == nullptr) return JNI_FALSE;

        jsize len = env->GetArrayLength(timestamps);
        jlong* ts = env->GetLongArrayElements(timestamps, nullptr);

        // Remove old timestamps
        vector<jlong> valid;
        for (jsize i = 0; i < len; i++) {
             if ((currentTime - ts[i]) <= (windowSeconds * 1000)) {
                valid.push_back(ts[i]);
            }
        }

        // Check min time between messages
        if (!valid.empty()) {
            jlong lastMsgTime = valid[valid.size() - 1];
            if ((currentTime - lastMsgTime) < minTime) {
                env->ReleaseLongArrayElements(timestamps, ts, 0);
                return JNI_TRUE;
            }
        }

        // Check max messages in window
        if (valid.size() >= maxMessages) {
            env->ReleaseLongArrayElements(timestamps, ts, 0);
            return JNI_TRUE;
        }

        env->ReleaseLongArrayElements(timestamps, ts, 0);
        return JNI_FALSE;
    }

    // Render format - fast string replacement in C++
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_renderFormatNative(
            JNIEnv* env, jclass clazz, jstring format, jobjectArray placeholders, jobjectArray values) {

        string result = jstringToString(env, format);

        jsize len = env->GetArrayLength(placeholders);
        for (jsize i = 0; i < len; i++) {
            jstring ph = (jstring)env->GetObjectArrayElement(placeholders, i);
            jstring val = (jstring)env->GetObjectArrayElement(values, i);

            if (ph != nullptr && val != nullptr) {
                string phStr = jstringToString(env, ph);
                string valStr = jstringToString(env, val);
                size_t pos = 0;
                while ((pos = result.find(phStr, pos)) != string::npos) {
                    result.replace(pos, phStr.length(), valStr);
                    pos += valStr.length();
                }
            }
        }

        return stringToJstring(env, result);
    }

    // Convert legacy color codes to MiniMessage format
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_convertLegacyToMiniMessageNative(
            JNIEnv* env, jclass clazz, jstring input) {

        string result = jstringToString(env, input);
        string output;

        for (size_t i = 0; i < result.length(); i++) {
            if (result[i] == '&' && i + 1 < result.length()) {
                string code = "&" + string(1, result[i + 1]);
                auto it = legacyToMini.find(code);
                if (it != legacyToMini.end()) {
                    output += it->second;
                    i++; // Skip the next character
                } else {
                    output += result[i];
                }
            } else {
                output += result[i];
            }
        }

        return stringToJstring(env, output);
    }

    // Validate and format UUID string
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_formatUUIDNative(
            JNIEnv* env, jclass clazz, jstring uuid) {

        string input = jstringToString(env, uuid);
        // Remove dashes and convert to lowercase
        string cleaned;
        for (char c : input) {
            if (c != '-') {
                cleaned += tolower(c);
            }
        }

        // Check if valid UUID (32 hex chars)
        if (cleaned.length() != 32) {
            return nullptr;
        }

        for (char c : cleaned) {
            if (!isxdigit(c)) {
                return nullptr;
            }
        }

        // Format as standard UUID
        if (cleaned.length() == 32) {
            string formatted = cleaned.substr(0, 8) + "-" +
                             cleaned.substr(8, 4) + "-" +
                             cleaned.substr(12, 4) + "-" +
                             cleaned.substr(16, 4) + "-" +
                             cleaned.substr(20, 12);
            return stringToJstring(env, formatted);
        }

        return nullptr;
    }

    // Fast string hashing (DJB2 algorithm)
    JNIEXPORT jint JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_hashStringNative(
            JNIEnv* env, jclass clazz, jstring input) {

        string str = jstringToString(env, input);
        unsigned long hash = 5381;
        for (char c : str) {
            hash = ((hash << 5) + hash) + c; // hash * 33 + c
        }
        return (jint)(hash & 0xFFFFFFFF);
    }

    // Parse MiniMessage gradient colors
    JNIEXPORT jobjectArray JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_parseGradientColorsNative(
            JNIEnv* env, jclass clazz, jstring gradientTag) {

        string tag = jstringToString(env, gradientTag);
        vector<string> colors;

        // Extract content between <gradient: and >
        size_t start = tag.find("<gradient:");
        if (start == string::npos) {
            return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
        }

        size_t end = tag.find(">", start);
        if (end == string::npos) {
            return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
        }

        string content = tag.substr(start + 10, end - start - 10);
        stringstream ss(content);
        string color;

        while (getline(ss, color, ':')) {
            if (!color.empty()) {
                colors.push_back(color);
            }
        }

        // Create Java array
        jobjectArray result = env->NewObjectArray(colors.size(), env->FindClass("java/lang/String"), nullptr);
        for (size_t i = 0; i < colors.size(); i++) {
            env->SetObjectArrayElement(result, i, stringToJstring(env, colors[i]));
        }

        return result;
    }

    // Check if message contains filtered content
    JNIEXPORT jboolean JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_containsFilteredContentNative(
            JNIEnv* env, jclass clazz, jstring message, jboolean filterItemNames) {

        string msg = jstringToString(env, message);
        transform(msg.begin(), msg.end(), msg.begin(), ::tolower);

        // Check for item placeholders if enabled
        if (filterItemNames && msg.find("[item]") != string::npos) {
            return JNI_TRUE;
        }

        // Check for URLs
        if (msg.find("http://") != string::npos || msg.find("https://") != string::npos) {
            return JNI_TRUE;
        }

        // Check for IP addresses (simple pattern)
        for (size_t i = 0; i < msg.length(); i++) {
            if (isdigit(msg[i])) {
                int dotCount = 0;
                size_t j = i;
                for (; j < msg.length() && (isdigit(msg[j]) || msg[j] == '.'); j++) {
                    if (msg[j] == '.') dotCount++;
                }
                if (dotCount == 3) {
                    return JNI_TRUE;
                }
                i = j;
            }
        }

        return JNI_FALSE;
    }

    // Convert string to lowercase (fast C++ implementation)
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_toLowerCaseNative(
            JNIEnv* env, jclass clazz, jstring input) {

        string str = jstringToString(env, input);
        transform(str.begin(), str.end(), str.begin(), ::tolower);
        return stringToJstring(env, str);
    }

    // Fast array containment check
    JNIEXPORT jboolean JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_arrayContainsNative(
            JNIEnv* env, jclass clazz, jobjectArray array, jstring target) {

        if (array == nullptr || target == nullptr) return JNI_FALSE;

        string targetStr = jstringToString(env, target);
        jsize len = env->GetArrayLength(array);

        for (jsize i = 0; i < len; i++) {
            jstring elem = (jstring)env->GetObjectArrayElement(array, i);
            if (elem != nullptr) {
                string elemStr = jstringToString(env, elem);
                if (elemStr == targetStr) {
                    return JNI_TRUE;
                }
            }
        }

        return JNI_FALSE;
    }

    // Parse chat format placeholders and return count
    JNIEXPORT jint JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_countPlaceholdersNative(
            JNIEnv* env, jclass clazz, jstring format, jstring placeholderPrefix) {

        if (format == nullptr || placeholderPrefix == nullptr) return 0;

        string fmt = jstringToString(env, format);
        string prefix = jstringToString(env, placeholderPrefix);

        int count = 0;
        size_t pos = 0;

        while ((pos = fmt.find(prefix, pos)) != string::npos) {
            count++;
            pos += prefix.length();
        }

        return count;
    }

    // Fast string concatenation for chat components
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_fastConcatNative(
            JNIEnv* env, jclass clazz, jobjectArray strings) {

        if (strings == nullptr) return stringToJstring(env, "");

        string result;
        jsize len = env->GetArrayLength(strings);

        for (jsize i = 0; i < len; i++) {
            jstring elem = (jstring)env->GetObjectArrayElement(strings, i);
            if (elem != nullptr) {
                result += jstringToString(env, elem);
            }
        }

        return stringToJstring(env, result);
    }

} // extern "C"
