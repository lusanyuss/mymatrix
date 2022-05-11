package com.youku.onetrace.plugin.trace

object TraceBuildConstants {
    const val APM_TRACE_CLASS = "com/tencent/matrix/trace/core/AppMethodBeat"
    const val APM_TRACE_ON_WINDOW_FOCUS_METHOD = "onWindowFocusChanged"
    const val APM_TRACE_ATTACH_BASE_CONTEXT = "attachBaseContext"
    const val APM_TRACE_ATTACH_BASE_CONTEXT_ARGS = "(Landroid/content/Context;)V"
    const val APM_TRACE_APPLICATION_ON_CREATE = "onCreate"
    const val APM_TRACE_APPLICATION_ON_CREATE_ARGS = "()V"
    const val APM_TRACE_ACTIVITY_CLASS = "android/app/Activity"
    const val APM_TRACE_V7_ACTIVITY_CLASS = "android/support/v7/app/AppCompatActivity"
    const val APM_TRACE_V4_ACTIVITY_CLASS = "android/support/v4/app/FragmentActivity"
    const val APM_TRACE_ANDROIDX_ACTIVITY_CLASS = "androidx/appcompat/app/AppCompatActivity"
    const val APM_TRACE_APPLICATION_CLASS = "android/app/Application"
    const val APM_TRACE_METHOD_BEAT_CLASS = "com/tencent/matrix/trace/core/AppMethodBeat"
    const val APM_TRACE_ON_WINDOW_FOCUS_METHOD_ARGS = "(Z)V"
    val UN_TRACE_CLASS = arrayOf("R.class", "R$", "Manifest", "BuildConfig")
    const val DEFAULT_BLOCK_TRACE = ("[package]\n" + "-keeppackage android/\n" + "-keeppackage com/tencent/matrix/\n")
    private const val METHOD_ID_MAX = 0xFFFFF
    const val METHOD_ID_DISPATCH = METHOD_ID_MAX - 1
}