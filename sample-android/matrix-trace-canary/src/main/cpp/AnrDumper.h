

// Author: leafjia@tencent.com
//
// AnrDumper.h

#ifndef LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_ANRDUMPER_H_
#define LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_ANRDUMPER_H_

#include "SignalHandler.h"
#include <functional>
#include <string>
#include <optional>
#include <jni.h>

namespace MatrixTracer {

class AnrDumper : public SignalHandler {
 public:
    AnrDumper(const char* anrTraceFile, const char* printTraceFile);
    virtual ~AnrDumper();

 private:
    void handleSignal(int sig, const siginfo_t *info, void *uc) final;
    void handleDebuggerSignal(int sig, const siginfo_t *info, void *uc) final;
    static void* nativeBacktraceCallback(void* arg);
};
}   // namespace MatrixTracer

#endif  // LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_ANRDUMPER_H_