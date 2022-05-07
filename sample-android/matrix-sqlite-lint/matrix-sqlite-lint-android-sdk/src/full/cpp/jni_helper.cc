

//
//

#include <stdlib.h>
#include <string.h>
#include "jni_helper.h"
#include "com_tencent_sqlitelint_util_SLog.h"

namespace sqlitelint {
    //jstring to char* (unicode to utf-8)
    char *jstringToChars(JNIEnv *env, jstring jstr) {
        if (jstr == nullptr) {
            return nullptr;
        }

        jboolean isCopy = JNI_FALSE;
        const char *str = env->GetStringUTFChars(jstr, &isCopy);
        char *ret = strdup(str);
        env->ReleaseStringUTFChars(jstr, str);
        return ret;
    }

    jstring charsToJstring(JNIEnv *env, const char *pat) {
        jclass strClass = env->FindClass("java/lang/String");
        jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");

        jbyteArray bytes;
        if (pat != nullptr) {
            bytes = env->NewByteArray(strlen(pat));
            env->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte *) pat);
        } else {
            bytes = env->NewByteArray(1);
            char ch[1] =
                    {0};
            env->SetByteArrayRegion(bytes, 0, 1, (jbyte *) ch);
        }
        jstring encoding = env->NewStringUTF("utf-8");

        jstring jstr = (jstring) env->NewObject(strClass, ctorID, bytes, encoding);
        env->DeleteLocalRef(strClass);
        env->DeleteLocalRef(bytes);
        env->DeleteLocalRef(encoding);

        return jstr;
    }
}
