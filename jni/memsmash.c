/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
#include <string.h>
#include <jni.h>

#include <android/log.h>
#define LOG(...) __android_log_print( ANDROID_LOG_ERROR, "MemSmash", __VA_ARGS__ )

char* leakedMem;
int leakedSize = 0;

jstring
Java_com_adcolony_memsmash_MemSmash_stringFromJNI(JNIEnv* env, jobject self)
{
     while (1) {
	  leakedMem = (char*)malloc(sizeof(char) * 1024 * 1024);
	  leakedSize++;
	  if (!leakedMem) {
	       LOG("*** leaked %d MB", leakedSize);
	       LOG("*** OOM");
	       break;
	  }
     }


     return (*env)->NewStringUTF(env, "Hello from the other side!");
}
