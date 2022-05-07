

#include "loader.h"

//
//

using namespace std;
struct JNIModule
{
	const char *name;
	ModuleInitializer func;
	bool init;

	JNIModule(const char *name_, ModuleInitializer func_, bool init_)
		: name(name_), func(func_), init(init_) {}
};
static vector<JNIModule> *g_loaders = nullptr;


void register_module_func(const char *name, ModuleInitializer func, int init)
{
	if (!g_loaders)
		g_loaders = new vector<JNIModule>();

	g_loaders->push_back(JNIModule(name, func, !!init));
}


extern "C" JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	JNIEnv* env;
	if (vm->GetEnv((void**) (&env), JNI_VERSION_1_6) != JNI_OK) {
        sqlitelint::LOGE( "Initialize GetEnv null");
		return -1;
	}

	vector<JNIModule>::iterator e = g_loaders->end();
	for (vector<JNIModule>::iterator it = g_loaders->begin(); it != e; ++it)
	{
		if (it->init)
		{
            sqlitelint::LOGI( "Initialize module '%s'...", it->name);
			if (it->func(vm, env) != 0)
				return -1;
		}
	}

	return JNI_VERSION_1_6;
}

extern "C" JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved)
{
	JNIEnv* env;
	if (vm->GetEnv((void**) (&env), JNI_VERSION_1_6) != JNI_OK) {
        sqlitelint::LOGE( "Finalize GetEnv null");
		return;
	}

	vector<JNIModule>::iterator e = g_loaders->end();
	for (vector<JNIModule>::iterator it = g_loaders->begin(); it != e; ++it)
	{
		if (!it->init)
		{
            sqlitelint::LOGI("Finalize module '%s'...", it->name);
			it->func(vm, env);
		}
	}
}
