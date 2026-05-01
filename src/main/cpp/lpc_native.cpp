#include "lpc_native.h"
#include <jni.h>
#include <string>
#include <vector>
#include <cctype>
#include <algorithm>
#include <chrono>

using namespace std;

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
            if ((currentTime - ts[i]) <= (windowSeconds * 1000L)) {
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

} // extern "C"
