package com.youku.onetrace.plugin.compat

import com.android.build.gradle.AppExtension
import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.plugin.trace.MatrixTraceInjection
import com.youku.onetrace.plugin.transform.MatrixTraceLegacyTransform
import com.youku.onetrace.plugin.trace.extension.ITraceSwitchListener
import com.youku.onetrace.plugin.trace.extension.MatrixTraceExtension
import org.gradle.api.Project

class MatrixTraceCompat : ITraceSwitchListener {
    
    companion object {
        const val TAG = "Matrix.TraceCompat"
        
        const val LEGACY_FLAG = "matrix_trace_legacy"
    }
    
    var traceInjection: MatrixTraceInjection? = null
    
    init {
        if(VersionsCompat.greatThanOrEqual(AGPVersion.AGP_4_0_0)) {
            traceInjection = MatrixTraceInjection()
        }
    }
    
    override fun onTraceEnabled(enable: Boolean) {
        traceInjection?.onTraceEnabled(enable)
    }
    
    fun inject(appExtension: AppExtension, project: Project, extension: MatrixTraceExtension) {
        when {
            VersionsCompat.lessThan(AGPVersion.AGP_3_6_0) -> legacyInject(appExtension, project, extension)
            VersionsCompat.greatThanOrEqual(AGPVersion.AGP_4_0_0) -> {
                if(project.extensions.extraProperties.has(LEGACY_FLAG) && (project.extensions.extraProperties.get(LEGACY_FLAG) as? String?) == "true") {
                    legacyInject(appExtension, project, extension)
                } else {
                    traceInjection!!.inject(appExtension, project, extension)
                }
            }
            else -> Log.e(
                TAG, "Matrix does not support Android Gradle Plugin " + "${VersionsCompat.androidGradlePluginVersion}!."
            )
        }
    }
    
    private fun legacyInject(
        appExtension: AppExtension, project: Project, extension: MatrixTraceExtension
    ) {
        
        project.afterEvaluate {
            
            if(!extension.isEnable) {
                return@afterEvaluate
            } // todo yuliu:  变体工件
            appExtension.applicationVariants.all {
                MatrixTraceLegacyTransform.inject(extension, project, it)
            }
        }
    }
}
