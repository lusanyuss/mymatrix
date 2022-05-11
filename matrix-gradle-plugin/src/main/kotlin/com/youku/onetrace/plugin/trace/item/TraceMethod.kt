package com.youku.onetrace.plugin.trace.item

import com.youku.onetrace.javalib.util.Util
import com.youku.onetrace.plugin.trace.retrace.MappingCollector
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 追踪的方法,能转成混淆方法
 */
class TraceMethod {
    var mId = 0
    var mAccessFlag = 0
    var mClassName: String? = null
    var mMethodName: String? = null
    var mDesc //返回类型,没有就为空
            : String? = null
    
    /**
     * 获取方法名称
     *
     * @return
     */
    fun getMethodName(): String {
        return if(mDesc == null || isNativeMethod) {
            mClassName + "." + mMethodName
        } else {
            mClassName + "." + mMethodName + "." + mDesc
        }
    }
    
    /**
     * proguard -> original
     *
     * @param processor
     */
    fun revert(processor: MappingCollector?) {
        if(null == processor) {
            return
        }
        val methodInfo = processor.originalMethodInfo(mClassName, mMethodName, mDesc)
        mMethodName = methodInfo!!.originalName
        mDesc = methodInfo.desc
        mClassName = processor.originalClassName(mClassName, mClassName)
    }
    
    /**
     * 对方法进行混淆
     *
     * @param processor
     */
    fun proguard(processor: MappingCollector?) {
        if(null == processor) {
            return
        }
        val methodInfo = processor.obfuscatedMethodInfo(mClassName, mMethodName, mDesc)
        mMethodName = methodInfo!!.originalName
        mDesc = methodInfo.desc
        mClassName = processor.proguardClassName(mClassName, mClassName)
    }
    
    /**
     * 获取返回值
     *
     * @return
     */
    val `return`: String?
        get() = if(Util.isNullOrNil(mDesc)) {
            null
        } else Type.getReturnType(mDesc).toString()
    
    /**
     * 转字符串
     *
     * @return
     */
    override fun toString(): String {
        return if(mDesc == null || isNativeMethod) {
            "$mId,$mAccessFlag,$mClassName $mMethodName"
        } else {
            "$mId,$mAccessFlag,$mClassName $mMethodName $mDesc"
        }
    }
    
    /**
     * 忽略方法字符串
     *
     * @return
     */
    fun toIgnoreString(): String {
        return if(mDesc == null || isNativeMethod) {
            "$mClassName $mMethodName"
        } else {
            "$mClassName $mMethodName $mDesc"
        }
    }
    
    /**
     * 判断是否native方法
     *
     * @return
     */
    val isNativeMethod: Boolean
        get() = mAccessFlag and Opcodes.ACC_NATIVE != 0
    
    /**
     * 冲洗equals和hashCode方法
     *
     * @param obj
     * @return
     */
    override fun equals(obj: Any?): Boolean {
        return if(obj is TraceMethod) {
            obj.getMethodName() == getMethodName()
        } else {
            false
        }
    }
    
    override fun hashCode(): Int {
        return super.hashCode()
    }
    
    companion object {
        private const val TAG = "Matrix.TraceMethod"
        fun create(id: Int, accessFlag: Int, className: String?, methodName: String?, desc: String?): TraceMethod {
            val traceMethod = TraceMethod()
            traceMethod.mId = id
            traceMethod.mAccessFlag = accessFlag
            traceMethod.mClassName = className!!.replace("/", ".")
            traceMethod.mMethodName = methodName
            traceMethod.mDesc = desc!!.replace("/", ".")
            return traceMethod
        }
    }
}