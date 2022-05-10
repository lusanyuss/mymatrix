/*
 * Tencent is pleased to support the open source community by making wechat-matrix available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 * Licensed under the BSD 3-Clause License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.matrix.trace.item;


import com.tencent.matrix.javalib.util.Util;
import com.tencent.matrix.trace.retrace.MappingCollector;
import com.tencent.matrix.trace.retrace.MethodInfo;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 追踪的方法,能转成混淆方法
 */
public class TraceMethod {
    private static final String TAG = "Matrix.TraceMethod";
    public int id;
    public int accessFlag;
    public String className;
    public String methodName;
    public String desc;//返回类型,没有就为空

    public static TraceMethod create(int id, int accessFlag, String className, String methodName, String desc) {
        TraceMethod traceMethod = new TraceMethod();
        traceMethod.id = id;
        traceMethod.accessFlag = accessFlag;
        traceMethod.className = className.replace("/", ".");
        traceMethod.methodName = methodName;
        traceMethod.desc = desc.replace("/", ".");
        return traceMethod;
    }

    /**
     * 获取方法名称
     *
     * @return
     */
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
     * 对方法进行混淆
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

    /**
     * 获取返回值
     *
     * @return
     */
    public String getReturn() {
        if (Util.isNullOrNil(desc)) {
            return null;
        }
        return Type.getReturnType(desc).toString();
    }


    /**
     * 转字符串
     *
     * @return
     */
    @Override
    public String toString() {
        if (desc == null || isNativeMethod()) {
            return id + "," + accessFlag + "," + className + " " + methodName;
        } else {
            return id + "," + accessFlag + "," + className + " " + methodName + " " + desc;
        }
    }

    /**
     * 忽略方法字符串
     *
     * @return
     */
    public String toIgnoreString() {
        if (desc == null || isNativeMethod()) {
            return className + " " + methodName;
        } else {
            return className + " " + methodName + " " + desc;
        }
    }

    /**
     * 判断是否native方法
     *
     * @return
     */
    public boolean isNativeMethod() {
        return (accessFlag & Opcodes.ACC_NATIVE) != 0;
    }

    /**
     * 冲洗equals和hashCode方法
     *
     * @param obj
     * @return
     */
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
