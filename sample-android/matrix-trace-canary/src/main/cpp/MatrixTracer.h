

// Author: leafjia@tencent.com
//
// MatrixTracer.h

#ifndef LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_MatrixTracer_H_
#define LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_MatrixTracer_H_

bool anrDumpCallback();
bool anrDumpTraceCallback();
bool nativeBacktraceDumpCallback();
bool printTraceCallback();
void hookAnrTraceWrite(bool isSigUser);
void unHookAnrTraceWrite();
void onTouchEventLag(int fd);
void onTouchEventLagDumpTrace(int fd);
#endif  // LAGDETECTOR_LAG_DETECTOR_MAIN_CPP_MatrixTracer_H_