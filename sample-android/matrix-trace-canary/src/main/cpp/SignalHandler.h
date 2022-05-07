

// Author: leafjia@tencent.com
//
// SignalHandler.h

#ifndef LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_SIGNALHANDLER_H_
#define LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_SIGNALHANDLER_H_

#include <signal.h>

namespace MatrixTracer {

class SignalHandler {
 public:
    SignalHandler();
    virtual ~SignalHandler();

 protected:
    enum Result { NOT_HANDLED = 0, HANDLED, HANDLED_NO_RETRIGGER };
    virtual void handleSignal(int sig, const siginfo_t *info, void *uc) = 0;
    virtual void handleDebuggerSignal(int sig, const siginfo_t *info, void *uc) = 0;
    static const int TARGET_SIG = SIGQUIT;
    static const int BIONIC_SIGNAL_DEBUGGER = (__SIGRTMIN + 3);
    static bool installHandlersLocked();
    static bool installNativeBacktraceHandlersLocked();
    static void restoreHandlersLocked();
    static void restoreNativeBacktraceHandlersLocked();
    static void installDefaultHandler(int sig);

 private:
    static void signalHandler(int sig, siginfo_t* info, void* uc);
    static void debuggerSignalHandler(int sig, siginfo_t* info, void* uc);


    SignalHandler(const SignalHandler &) = delete;
    SignalHandler &operator= (const SignalHandler &) = delete;
};

}   // namespace MatrixTracer

#endif  // LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_SIGNALHANDLER_H_
