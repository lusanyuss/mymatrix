

#ifndef __LIB_SQLITELINT_LOADER_H__
#define __LIB_SQLITELINT_LOADER_H__

#include <jni.h>
#include "com_tencent_sqlitelint_util_SLog.h"
#include <vector>

//
//

#define MODULE_INIT(name) \
	static int ModuleInit_##name(JavaVM *vm, JNIEnv *env); \
	static void __attribute__((constructor)) MODULE_INIT_##name() \
	{ \
		register_module_func(#name, ModuleInit_##name, 1); \
	} \
	static int ModuleInit_##name(JavaVM *vm, JNIEnv *env)

#define MODULE_FINI(name) \
	static int ModuleFini_##name(JavaVM *vm, JNIEnv *env); \
	static void __attribute__((constructor)) MODULE_FINI_##name() \
	{ \
		register_module_func(#name, ModuleFini_##name, 0); \
	} \
	static int ModuleFini_##name(JavaVM *vm, JNIEnv *env)


#ifdef __cplusplus
#define SQLITELINT_EXPORT extern "C"
#else
#define WECHAT_EXPORT
#endif

typedef int (*ModuleInitializer)(JavaVM *vm, JNIEnv *env);
SQLITELINT_EXPORT void register_module_func(const char *name, ModuleInitializer func, int init);


#endif
