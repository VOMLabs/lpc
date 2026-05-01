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

}

#endif // LPC_NATIVE_H
