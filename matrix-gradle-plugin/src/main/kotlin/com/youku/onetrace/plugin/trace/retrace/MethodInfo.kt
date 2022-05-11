package com.youku.onetrace.plugin.trace.retrace

class MethodInfo {
    val originalClassName: String?
    var originalType: String?
    var originalArguments: String
    var originalName: String?
    var desc: String? = null
    
    
    constructor(
        originalClassName: String?, originalType: String?, originalName: String?, originalArguments: String
    ) {
        this.originalType = originalType
        this.originalArguments = originalArguments
        this.originalClassName = originalClassName
        this.originalName = originalName
    }
    
    constructor(methodInfo: MethodInfo) {
        originalType = methodInfo.originalType
        originalArguments = methodInfo.originalArguments
        originalClassName = methodInfo.originalClassName
        originalName = methodInfo.originalName
        desc = methodInfo.desc
    }
    
    fun matches(originalType: String?, originalArguments: String?): Boolean {
        return (originalType == null || originalType == this.originalType) && (originalArguments == null || originalArguments == this.originalArguments)
    }
    
    companion object {
        fun deFault(): MethodInfo {
            return MethodInfo("", "", "", "")
        }
    }
}