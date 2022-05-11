package com.youku.onetrace.plugin.trace.extension

class ApmTraceExtension {
    var isTransformInjectionForced = false
    var baseMethodMapFile: String? = null
    var blackListFile: String? = null
    var customDexTransformName: String? = null
    var isSkipCheckClass = true // skip by default
    var isEnable = false
}