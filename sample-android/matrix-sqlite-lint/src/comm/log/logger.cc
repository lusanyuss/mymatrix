

#include "logger.h"
#include <stdio.h>




namespace sqlitelint {
	static SLogFunc kLogfunc;
    SLogLevel kLogLevel = kLevelVerbose;

	static int dummyLog(int prio, const char *msg){
		return 0;
	}


	void SetSLogFunc(SLogFunc func){
		if (!func) func = dummyLog;
		kLogfunc = func;
	}

	void SetSLogLevel(SLogLevel level){
		kLogLevel = level;
	}


	int SLog(int prio, const char *fmt, ...){
		if (prio < kLogLevel){
			return -1;
		}
		if (prio < kLogLevel){
			prio = kLogLevel;
		}
		va_list ap;
		char buf[1024];

		va_start(ap, fmt);
		vsnprintf(buf, sizeof(buf), fmt, ap);
		va_end(ap);
		if (!kLogfunc){
			return dummyLog(prio, buf);
		}

		return kLogfunc(prio, buf);
	}
}
