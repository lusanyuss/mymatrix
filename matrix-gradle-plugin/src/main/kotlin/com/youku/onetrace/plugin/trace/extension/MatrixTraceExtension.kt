package com.youku.onetrace.plugin.trace.extension

class MatrixTraceExtension {
    var isTransformInjectionForced = false
    
    //    public void setEnable(boolean enable) {
    //        this.enable = enable;
    //        onTraceEnabled(enable);
    //    }
    var baseMethodMapFile: String? = null
    var blackListFile: String? = null
    var customDexTransformName: String? = null
    var isSkipCheckClass = true // skip by default
    var isEnable = false
}