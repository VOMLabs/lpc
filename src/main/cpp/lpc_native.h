#ifndef LPC_NATIVE_H
#define LPC_NATIVE_H

#include <jni.h>
#include <string>
#include <vector>
#include <cctype>
#include <algorithm>

// LPC Native API - C++ implementation for performance-critical parts

extern "C" {

    // Filter swear words in a string
    // Returns a new filtered string
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_filterSwearWordsNative(
        JNIEnv* env, jclass clazz, jstring input, jobjectArray words, jstring replacement);

    // Check if a message is spam (fast C++ implementation)
    JNIEXPORT jboolean JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_isSpamNative(
        JNIEnv* env, jclass clazz, jlongArray timestamps, jlong currentTime, jlong minTime, jint maxMessages, jint windowSeconds);

    // Render chat format (fast string replacement in C++)
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_renderFormatNative(
        JNIEnv* env, jclass clazz, jstring format, jobjectArray placeholders, jobjectArray values);

    // Convert legacy color codes (&a, &b, etc.) to MiniMessage format
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_convertLegacyToMiniMessageNative(
        JNIEnv* env, jclass clazz, jstring input);

    // Validate and format UUID string
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_formatUUIDNative(
        JNIEnv* env, jclass clazz, jstring uuid);

    // Fast string hashing (for caching chat formats)
    JNIEXPORT jint JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_hashStringNative(
        JNIEnv* env, jclass clazz, jstring input);

    // Parse MiniMessage gradient tag (extract colors)
    JNIEXPORT jobjectArray JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_parseGradientColorsNative(
        JNIEnv* env, jclass clazz, jstring gradientTag);

    // Fast message filtering (check if message contains filtered content)
    JNIEXPORT jboolean JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_containsFilteredContentNative(
        JNIEnv* env, jclass clazz, jstring message, jboolean filterItemNames);

    // Convert string to lowercase (fast C++ implementation)
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_toLowerCaseNative(
        JNIEnv* env, jclass clazz, jstring input);

    // Fast array containment check
    JNIEXPORT jboolean JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_arrayContainsNative(
        JNIEnv* env, jclass clazz, jobjectArray array, jstring target);

    // Parse chat format placeholders and return count
    JNIEXPORT jint JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_countPlaceholdersNative(
        JNIEnv* env, jclass clazz, jstring format, jstring placeholderPrefix);

    // Fast string concatenation for chat components
    JNIEXPORT jstring JNICALL Java_com_vomlabs_lpcmmx_native_NativeAPI_fastConcatNative(
        JNIEnv* env, jclass clazz, jobjectArray strings);

}

#endif // LPC_NATIVE_H
