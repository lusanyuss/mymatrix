package com.youku.apm.plugin.trace.extension

interface ITraceSwitchListener {
    fun onTraceEnabled(enable: Boolean)
}