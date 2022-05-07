

//
//

#ifndef SQLITELINT_JNIHELPER_H
#define SQLITELINT_JNIHELPER_H


#include <jni.h>

namespace sqlitelint {
    char *jstringToChars(JNIEnv *env, jstring jstr);
    jstring charsToJstring(JNIEnv* env, const char* pat);
}

#endif //SQLITELINT_JNIHELPER_H
