
//



#include <jni.h>
#include "xhook.h"

#define JNI_API_DEF(f) Java_com_qiyi_xhook_NativeHandler_##f

JNIEXPORT jint JNI_API_DEF(refresh)(JNIEnv *env, jobject obj, jboolean async)
{
    (void)env;
    (void)obj;

    return xhook_refresh(async ? 1 : 0);
}

JNIEXPORT void JNI_API_DEF(clear)(JNIEnv *env, jobject obj)
{
    (void)env;
    (void)obj;

    xhook_clear();
}

JNIEXPORT void JNI_API_DEF(enableDebug)(JNIEnv *env, jobject obj, jboolean flag)
{
    (void)env;
    (void)obj;

    xhook_enable_debug(flag ? 1 : 0);
}

JNIEXPORT void JNI_API_DEF(enableSigSegvProtection)(JNIEnv *env, jobject obj, jboolean flag)
{
    (void)env;
    (void)obj;

    xhook_enable_sigsegv_protection(flag ? 1 : 0);
}
