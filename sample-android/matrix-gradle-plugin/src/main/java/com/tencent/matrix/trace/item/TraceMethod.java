

package com.tencent.matrix.trace.item;


import com.tencent.matrix.javalib.util.Util;
import com.tencent.matrix.trace.retrace.MappingCollector;
import com.tencent.matrix.trace.retrace.MethodInfo;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class TraceMethod {
    private static final String TAG = "Matrix.TraceMethod";
    public int id;
    public int accessFlag;
    public String className;
    public String methodName;
    public String desc;

    public static TraceMethod create(int id, int accessFlag, String className, String methodName, String desc) {
        TraceMethod traceMethod = new TraceMethod();
        traceMethod.id = id;
        traceMethod.accessFlag = accessFlag;
        traceMethod.className = className.replace("/", ".");
        traceMethod.methodName = methodName;
        traceMethod.desc = desc.replace("/", ".");
        return traceMethod;
    }

    public String getMethodName() {
        if (desc == null || isNativeMethod()) {
            return this.className + "." + this.methodName;
        } else {
            return this.className + "." + this.methodName + "." + desc;
        }
    }

    /**
     * proguard -> original
     *
     * @param processor
     */
    public void revert(MappingCollector processor) {
        if (null == processor) {
            return;
        }
        MethodInfo methodInfo = processor.originalMethodInfo(className, methodName, desc);
        this.methodName = methodInfo.originalName;
        this.desc = methodInfo.desc;
        this.className = processor.originalClassName(className, className);
    }

    /**
     * original -> proguard
     *
     * @param processor
     */
    public void proguard(MappingCollector processor) {
        if (null == processor) {
            return;
        }
        MethodInfo methodInfo = processor.obfuscatedMethodInfo(className, methodName, desc);
        this.methodName = methodInfo.originalName;
        this.desc = methodInfo.desc;
        this.className = processor.proguardClassName(className, className);
    }

    public String getReturn() {
        if (Util.isNullOrNil(desc)) {
            return null;
        }
        return Type.getReturnType(desc).toString();
    }


    @Override
    public String toString() {
        if (desc == null || isNativeMethod()) {
            return id + "," + accessFlag + "," + className + " " + methodName;
        } else {
            return id + "," + accessFlag + "," + className + " " + methodName + " " + desc;
        }
    }

    public String toIgnoreString() {
        if (desc == null || isNativeMethod()) {
            return className + " " + methodName;
        } else {
            return className + " " + methodName + " " + desc;
        }
    }

    public boolean isNativeMethod() {
        return (accessFlag & Opcodes.ACC_NATIVE) != 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TraceMethod) {
            TraceMethod tm = (TraceMethod) obj;
            return tm.getMethodName().equals(getMethodName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
