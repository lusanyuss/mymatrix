package com.youku.apm.plugin.trace.retrace

import com.youku.apm.javalib.util.Log
import org.objectweb.asm.Type

class MappingCollector : MappingProcessor {
    //混淆类名->原始类名 映射
    var mObfuscatedRawClassMap = HashMap<String, String?>(DEFAULT_CAPACITY)
    
    //原始类名->混淆类名 映射
    var mRawObfuscatedClassMap = HashMap<String, String?>(DEFAULT_CAPACITY)
    
    //原始包->混淆包 映射
    var mRawObfuscatedPackageMap = HashMap<String, String?>(DEFAULT_CAPACITY)
    
    //混淆类名->方法映射
    private val mObfuscatedClassMethodMap: MutableMap<String?, MutableMap<String, MutableSet<MethodInfo>>> = HashMap()
    
    //原始类方法映射
    private val mOriginalClassMethodMap: MutableMap<String?, MutableMap<String, MutableSet<MethodInfo>>?> = HashMap()
    
    /**
     * 主要是保存映射关系
     *
     * @param className    the original class name.
     * @param newClassName the new class name.
     * @return
     */
    override fun processClassMapping(className: String, newClassName: String): Boolean {
        mObfuscatedRawClassMap[newClassName] = className
        mRawObfuscatedClassMap[className] = newClassName
        val classNameLen = className.lastIndexOf('.')
        val newClassNameLen = newClassName.lastIndexOf('.')
        if(classNameLen > 0 && newClassNameLen > 0) {
            mRawObfuscatedPackageMap[className.substring(0, classNameLen)] = newClassName.substring(0, newClassNameLen)
        } else {
            Log.e(TAG, "class without package name: %s -> %s, pls check input mapping", className, newClassName)
        }
        return true
    }
    
    /**
     * 保存  混淆类->混淆方法->混淆方法
     * 和   类->方法->方法 信息映射关系
     *
     * @param className        the original class name.
     * @param methodReturnType the original external method return type.
     * @param methodName       the original external method name.
     * @param methodArguments  the original external method arguments.
     * @param newClassName     the new class name.
     * @param newMethodName    the new method name.
     */
    override fun processMethodMapping(className: String, methodReturnType: String?, methodName: String, methodArguments: String, newClassName: String?, newMethodName: String) {
        var newClassName = newClassName
        newClassName = mRawObfuscatedClassMap[className]
        var methodMap = mObfuscatedClassMethodMap[newClassName]
        if(methodMap == null) {
            methodMap = HashMap()
            mObfuscatedClassMethodMap[newClassName] = methodMap
        }
        var methodSet = methodMap[newMethodName]
        if(methodSet == null) {
            methodSet = LinkedHashSet()
            methodMap[newMethodName] = methodSet
        }
        methodSet.add(MethodInfo(className, methodReturnType, methodName, methodArguments))
        var methodMap2 = mOriginalClassMethodMap[className]
        if(methodMap2 == null) {
            methodMap2 = HashMap()
            mOriginalClassMethodMap[className] = methodMap2
        }
        var methodSet2 = methodMap2[methodName]
        if(methodSet2 == null) {
            methodSet2 = LinkedHashSet()
            methodMap2[methodName] = methodSet2
        }
        methodSet2.add(MethodInfo(newClassName, methodReturnType, newMethodName, methodArguments))
    }
    
    /**
     * 获取原始类名
     *
     * @param proguardClassName
     * @param defaultClassName
     * @return
     */
    fun originalClassName(proguardClassName: String?, defaultClassName: String?): String? {
        return if(mObfuscatedRawClassMap.containsKey(proguardClassName)) {
            mObfuscatedRawClassMap[proguardClassName]
        } else {
            defaultClassName
        }
    }
    
    /**
     * 获取混淆类名
     *
     * @param originalClassName
     * @param defaultClassName
     * @return
     */
    fun proguardClassName(originalClassName: String?, defaultClassName: String?): String? {
        return if(mRawObfuscatedClassMap.containsKey(originalClassName)) {
            mRawObfuscatedClassMap[originalClassName]
        } else {
            defaultClassName
        }
    }
    
    /**
     * 获取混淆包名
     *
     * @param originalPackage
     * @param defaultPackage
     * @return
     */
    fun proguardPackageName(originalPackage: String?, defaultPackage: String?): String? {
        return if(mRawObfuscatedPackageMap.containsKey(originalPackage)) {
            mRawObfuscatedPackageMap[originalPackage]
        } else {
            defaultPackage
        }
    }
    
    /**
     * get original method info
     * 获取原始方法信息
     *
     * @param obfuscatedClassName
     * @param obfuscatedMethodName
     * @param obfuscatedMethodDesc
     * @return
     */
    fun originalMethodInfo(obfuscatedClassName: String?, obfuscatedMethodName: String?, obfuscatedMethodDesc: String?): MethodInfo {
        val descInfo = parseMethodDesc(obfuscatedMethodDesc, false)
        
        // obfuscated name -> original method names.
        val methodMap: MutableMap<String, MutableSet<MethodInfo>>? = mObfuscatedClassMethodMap[obfuscatedClassName]
        if(methodMap != null) {
            val methodSet: Set<MethodInfo>? = methodMap[obfuscatedMethodName]
            if(methodSet != null) { // Find all matching methods.
                val methodInfoIterator = methodSet.iterator()
                while (methodInfoIterator.hasNext()) {
                    val methodInfo = methodInfoIterator.next()
                    if(methodInfo.matches(descInfo.mReturnType, descInfo.mArguments)) {
                        val newMethodInfo = MethodInfo(methodInfo)
                        newMethodInfo.desc=descInfo.mDesc
                        return newMethodInfo
                    }
                }
            }
        }
        val defaultMethodInfo: MethodInfo = MethodInfo.Companion.deFault()
        defaultMethodInfo.desc=descInfo.mDesc
        defaultMethodInfo.originalName=(obfuscatedMethodName)
        return defaultMethodInfo
    }
    
    /**
     * get obfuscated method info
     * 获取混淆方法信息
     * @param originalClassName
     * @param originalMethodName
     * @param originalMethodDesc
     * @return
     */
    fun obfuscatedMethodInfo(originalClassName: String?, originalMethodName: String?, originalMethodDesc: String?): MethodInfo {
        val descInfo = parseMethodDesc(originalMethodDesc, true)
        
        // Class name -> obfuscated method names.
        val methodMap: MutableMap<String, MutableSet<MethodInfo>>? = mOriginalClassMethodMap.get(originalClassName)
        if(methodMap != null) {
            val methodSet = methodMap[originalMethodName]
            if(null != methodSet) { // Find all matching methods.
                val methodInfoIterator = methodSet.iterator()
                while (methodInfoIterator.hasNext()) {
                    val methodInfo = methodInfoIterator.next()
                    val newMethodInfo = MethodInfo(methodInfo)
                    obfuscatedMethodInfo(newMethodInfo)
                    if(newMethodInfo.matches(descInfo.mReturnType, descInfo.mArguments)) {
                        newMethodInfo.desc=descInfo.mDesc
                        return newMethodInfo
                    }
                }
            }
        }
        val defaultMethodInfo: MethodInfo = MethodInfo.Companion.deFault()
        defaultMethodInfo.desc=descInfo.mDesc
        defaultMethodInfo.originalName=(originalMethodName)
        return defaultMethodInfo
    }
    
    private fun obfuscatedMethodInfo(methodInfo: MethodInfo) {
        val methodArguments = methodInfo.originalArguments
        val args = methodArguments!!.split(",").toTypedArray()
        val stringBuffer = StringBuffer()
        for (str in args) {
            val key = str.replace("[", "").replace("]", "")
            if(mRawObfuscatedClassMap.containsKey(key)) {
                stringBuffer.append(str.replace(key, mRawObfuscatedClassMap[key]!!,false))
            } else {
                stringBuffer.append(str)
            }
            stringBuffer.append(',')
        }
        if(stringBuffer.length > 0) {
            stringBuffer.deleteCharAt(stringBuffer.length - 1)
        }
        var methodReturnType = methodInfo.originalType
        val key = methodReturnType!!.replace("[", "").replace("]", "")
        if(mRawObfuscatedClassMap.containsKey(key)) {
            methodReturnType = methodReturnType.replace(key, mRawObfuscatedClassMap[key]!!,false)
        }
        methodInfo.originalArguments=(stringBuffer.toString())
        methodInfo.originalType=(methodReturnType)
    }
    
    /**
     * parse method desc
     *
     * @param desc
     * @param isRawToObfuscated
     * @return
     */
    private fun parseMethodDesc(desc: String?, isRawToObfuscated: Boolean): DescInfo {
        val descInfo = DescInfo()
        val argsObj = Type.getArgumentTypes(desc)
        val argumentsBuffer = StringBuffer()
        val descBuffer = StringBuffer()
        descBuffer.append('(')
        for (type in argsObj) {
            val key = type.className.replace("[", "").replace("]", "")
            if(isRawToObfuscated) {
                if(mRawObfuscatedClassMap.containsKey(key)) {
                    argumentsBuffer.append(type.className.replace(key, mRawObfuscatedClassMap[key]!!,false))
                    descBuffer.append(type.toString().replace(key, mRawObfuscatedClassMap[key]!!,false))
                } else {
                    argumentsBuffer.append(type.className)
                    descBuffer.append(type.toString())
                }
            } else {
                if(mObfuscatedRawClassMap.containsKey(key)) {
                    argumentsBuffer.append(type.className.replace(key, mObfuscatedRawClassMap[key]!!,false))
                    descBuffer.append(type.toString().replace(key, mObfuscatedRawClassMap[key]!!,false))
                } else {
                    argumentsBuffer.append(type.className)
                    descBuffer.append(type.toString())
                }
            }
            argumentsBuffer.append(',')
        }
        descBuffer.append(')')
        val returnObj: Type
        returnObj = try {
            Type.getReturnType(desc)
        } catch (e: ArrayIndexOutOfBoundsException) {
            Type.getReturnType("$desc;")
        }
        if(isRawToObfuscated) {
            val key = returnObj.className.replace("[", "").replace("]", "")
            if(mRawObfuscatedClassMap.containsKey(key)) {
                descInfo.setReturnType(returnObj.className.replace(key, mRawObfuscatedClassMap[key]!!,false))
                descBuffer.append(returnObj.toString().replace(key, mRawObfuscatedClassMap[key]!!,false))
            } else {
                descInfo.setReturnType(returnObj.className)
                descBuffer.append(returnObj.toString())
            }
        } else {
            val key = returnObj.className.replace("[", "").replace("]", "")
            if(mObfuscatedRawClassMap.containsKey(key)) {
                descInfo.setReturnType(returnObj.className.replace(key, mObfuscatedRawClassMap[key]!!,false))
                descBuffer.append(returnObj.toString().replace(key, mObfuscatedRawClassMap[key]!!,false))
            } else {
                descInfo.setReturnType(returnObj.className)
                descBuffer.append(returnObj.toString())
            }
        }
        
        // delete last ,
        if(argumentsBuffer.length > 0) {
            argumentsBuffer.deleteCharAt(argumentsBuffer.length - 1)
        }
        descInfo.setArguments(argumentsBuffer.toString())
        descInfo.mDesc=(descBuffer.toString())
        return descInfo
    }
    
    /**
     * about method desc info
     */
    private class DescInfo {
        var mDesc: String? = null
        var mArguments: String? = null
        var mReturnType: String? = null
        
        fun setArguments(arguments: String?) {
            this.mArguments = arguments
        }
        
        fun setReturnType(returnType: String?) {
            this.mReturnType = returnType
        }
        
        fun setDesc(desc: String?) {
            this.mDesc = desc
        }
    }
    
    companion object {
        private const val TAG = "MappingCollector"
        private const val DEFAULT_CAPACITY = 2000
    }
}