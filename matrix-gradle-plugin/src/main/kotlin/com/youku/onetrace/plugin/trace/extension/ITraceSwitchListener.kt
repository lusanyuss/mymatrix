package com.youku.onetrace.plugin.trace.extension

interface ITraceSwitchListener {
    fun onTraceEnabled(enable: Boolean)
}