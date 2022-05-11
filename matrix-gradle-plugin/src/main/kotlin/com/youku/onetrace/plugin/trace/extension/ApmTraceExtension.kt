package com.youku.onetrace.plugin.trace.extension

open class ApmTraceExtension {
    var transformInjectionForced = false
    var baseMethodMapFile: String? = null
    var blackListFile: String? = null
    var customDexTransformName: String? = null
    var skipCheckClass = true // skip by default
    var enable = false
}