// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.


// This file is autogenerated by
//     base/android/jni_generator/jni_generator.py
// For
//     org/mediasoup/droid/DataConsumer

#ifndef org_mediasoup_droid_DataConsumer_JNI
#define org_mediasoup_droid_DataConsumer_JNI

#include <jni.h>

#include "../include/jni_generator_helper.h"


// Step 1: Forward declarations.

JNI_REGISTRATION_EXPORT extern const char kClassPath_org_mediasoup_droid_DataConsumer[];
const char kClassPath_org_mediasoup_droid_DataConsumer[] = "org/mediasoup/droid/DataConsumer";

JNI_REGISTRATION_EXPORT extern const char
    kClassPath_org_mediasoup_droid_DataConsumer_00024DataConsumerListener[];
const char kClassPath_org_mediasoup_droid_DataConsumer_00024DataConsumerListener[] =
    "org/mediasoup/droid/DataConsumer$Listener";
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_REGISTRATION_EXPORT std::atomic<jclass> g_org_mediasoup_droid_DataConsumer_clazz(nullptr);
#ifndef org_mediasoup_droid_DataConsumer_clazz_defined
#define org_mediasoup_droid_DataConsumer_clazz_defined
inline jclass org_mediasoup_droid_DataConsumer_clazz(JNIEnv* env) {
  return base::android::LazyGetClass(env, kClassPath_org_mediasoup_droid_DataConsumer,
      &g_org_mediasoup_droid_DataConsumer_clazz);
}
#endif
// Leaking this jclass as we cannot use LazyInstance from some threads.
JNI_REGISTRATION_EXPORT std::atomic<jclass>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(nullptr);
#ifndef org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz_defined
#define org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz_defined
inline jclass org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(JNIEnv* env) {
  return base::android::LazyGetClass(env,
      kClassPath_org_mediasoup_droid_DataConsumer_00024DataConsumerListener,
      &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz);
}
#endif


// Step 2: Constants (optional).


// Step 3: Method stubs.
namespace mediasoupclient {

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetId(JNIEnv* env, jlong
    consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetId(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetId(env, consumer).Release();
}

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetDataProducerId(JNIEnv* env,
    jlong consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetDataProducerId(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetDataProducerId(env, consumer).Release();
}

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetLocalId(JNIEnv* env, jlong
    consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetLocalId(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetLocalId(env, consumer).Release();
}

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetSctpStreamParameters(JNIEnv*
    env, jlong consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetSctpStreamParameters(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetSctpStreamParameters(env, consumer).Release();
}

static jint JNI_DataConsumer_GetReadyState(JNIEnv* env, jlong consumer);

JNI_GENERATOR_EXPORT jint Java_org_mediasoup_droid_DataConsumer_nativeGetReadyState(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetReadyState(env, consumer);
}

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetLabel(JNIEnv* env, jlong
    consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetLabel(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetLabel(env, consumer).Release();
}

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetProtocol(JNIEnv* env, jlong
    consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetProtocol(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetProtocol(env, consumer).Release();
}

static base::android::ScopedJavaLocalRef<jstring> JNI_DataConsumer_GetAppData(JNIEnv* env, jlong
    consumer);

JNI_GENERATOR_EXPORT jstring Java_org_mediasoup_droid_DataConsumer_nativeGetAppData(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_GetAppData(env, consumer).Release();
}

static jboolean JNI_DataConsumer_IsClosed(JNIEnv* env, jlong consumer);

JNI_GENERATOR_EXPORT jboolean Java_org_mediasoup_droid_DataConsumer_nativeIsClosed(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_IsClosed(env, consumer);
}

static void JNI_DataConsumer_Close(JNIEnv* env, jlong consumer);

JNI_GENERATOR_EXPORT void Java_org_mediasoup_droid_DataConsumer_nativeClose(
    JNIEnv* env,
    jclass jcaller,
    jlong consumer) {
  return JNI_DataConsumer_Close(env, consumer);
}


static std::atomic<jmethodID>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnConnecting(nullptr);
static void Java_DataConsumerListener_OnConnecting(JNIEnv* env, const
    base::android::JavaRef<jobject>& obj, const base::android::JavaRef<jobject>& dataConsumer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env));

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "OnConnecting",
          "(Lorg/mediasoup/droid/DataConsumer;)V",
          &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnConnecting);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, dataConsumer.obj());
}

static std::atomic<jmethodID>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnOpen(nullptr);
static void Java_DataConsumerListener_OnOpen(JNIEnv* env, const base::android::JavaRef<jobject>&
    obj, const base::android::JavaRef<jobject>& dataConsumer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env));

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "OnOpen",
          "(Lorg/mediasoup/droid/DataConsumer;)V",
          &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnOpen);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, dataConsumer.obj());
}

static std::atomic<jmethodID>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnClosing(nullptr);
static void Java_DataConsumerListener_OnClosing(JNIEnv* env, const base::android::JavaRef<jobject>&
    obj, const base::android::JavaRef<jobject>& dataConsumer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env));

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "OnClosing",
          "(Lorg/mediasoup/droid/DataConsumer;)V",
          &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnClosing);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, dataConsumer.obj());
}

static std::atomic<jmethodID>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnClose(nullptr);
static void Java_DataConsumerListener_OnClose(JNIEnv* env, const base::android::JavaRef<jobject>&
    obj, const base::android::JavaRef<jobject>& dataConsumer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env));

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "OnClose",
          "(Lorg/mediasoup/droid/DataConsumer;)V",
          &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnClose);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, dataConsumer.obj());
}

static std::atomic<jmethodID>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnMessage(nullptr);
static void Java_DataConsumerListener_OnMessage(JNIEnv* env, const base::android::JavaRef<jobject>&
    obj, const base::android::JavaRef<jobject>& dataConsumer,
    const base::android::JavaRef<jobject>& buffer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env));

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "OnMessage",
          "(Lorg/mediasoup/droid/DataConsumer;Lorg/webrtc/DataChannel$Buffer;)V",
          &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnMessage);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, dataConsumer.obj(), buffer.obj());
}

static std::atomic<jmethodID>
    g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnTransportClose(nullptr);
static void Java_DataConsumerListener_OnTransportClose(JNIEnv* env, const
    base::android::JavaRef<jobject>& obj, const base::android::JavaRef<jobject>& dataConsumer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env);
  CHECK_CLAZZ(env, obj.obj(),
      org_mediasoup_droid_DataConsumer_00024DataConsumerListener_clazz(env));

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "OnTransportClose",
          "(Lorg/mediasoup/droid/DataConsumer;)V",
          &g_org_mediasoup_droid_DataConsumer_00024DataConsumerListener_OnTransportClose);

     env->CallVoidMethod(obj.obj(),
          call_context.base.method_id, dataConsumer.obj());
}

static std::atomic<jmethodID> g_org_mediasoup_droid_DataConsumer_generateBuffer(nullptr);
static base::android::ScopedJavaLocalRef<jobject> Java_DataConsumer_generateBuffer(JNIEnv* env,
    const base::android::JavaRef<jobject>& data,
    jboolean binary) {
  jclass clazz = org_mediasoup_droid_DataConsumer_clazz(env);
  CHECK_CLAZZ(env, clazz,
      org_mediasoup_droid_DataConsumer_clazz(env), NULL);

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_STATIC>(
          env,
          clazz,
          "generateBuffer",
          "(Ljava/nio/ByteBuffer;Z)Lorg/webrtc/DataChannel$Buffer;",
          &g_org_mediasoup_droid_DataConsumer_generateBuffer);

  jobject ret =
      env->CallStaticObjectMethod(clazz,
          call_context.base.method_id, data.obj(), binary);
  return base::android::ScopedJavaLocalRef<jobject>(env, ret);
}

static std::atomic<jmethodID> g_org_mediasoup_droid_DataConsumer_Constructor(nullptr);
static base::android::ScopedJavaLocalRef<jobject> Java_DataConsumer_Constructor(JNIEnv* env, jlong
    nativeConsumer) {
  jclass clazz = org_mediasoup_droid_DataConsumer_clazz(env);
  CHECK_CLAZZ(env, clazz,
      org_mediasoup_droid_DataConsumer_clazz(env), NULL);

  jni_generator::JniJavaCallContextChecked call_context;
  call_context.Init<
      base::android::MethodID::TYPE_INSTANCE>(
          env,
          clazz,
          "<init>",
          "(J)V",
          &g_org_mediasoup_droid_DataConsumer_Constructor);

  jobject ret =
      env->NewObject(clazz,
          call_context.base.method_id, nativeConsumer);
  return base::android::ScopedJavaLocalRef<jobject>(env, ret);
}

}  // namespace mediasoupclient

#endif  // org_mediasoup_droid_DataConsumer_JNI
