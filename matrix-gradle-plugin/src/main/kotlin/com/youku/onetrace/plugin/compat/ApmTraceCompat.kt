package com.youku.onetrace.plugin.compat

import com.android.build.gradle.AppExtension
import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.plugin.trace.ApmTraceInjection
import com.youku.onetrace.plugin.trace.extension.ApmTraceExtension
import com.youku.onetrace.plugin.trace.extension.ITraceSwitchListener
import com.youku.onetrace.plugin.transform.ApmTraceLegacyTransform
import org.gradle.api.Project

class ApmTraceCompat : ITraceSwitchListener {
    
    companion object {
        const val TAG = "Apm.TraceCompat"
        
        const val LEGACY_FLAG = "apm_trace_legacy"
    }
    
    var traceInjection: ApmTraceInjection? = null
    
    init {
        if(VersionsCompat.greatThanOrEqual(AGPVersion.AGP_4_0_0)) {
            traceInjection = ApmTraceInjection()
        }
    }
    
    override fun onTraceEnabled(enable: Boolean) {
        traceInjection?.onTraceEnabled(enable)
    }
    
    fun inject(appExtension: AppExtension, project: Project, extension: ApmTraceExtension) {
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
                TAG, "Apm does not support Android Gradle Plugin " + "${VersionsCompat.androidGradlePluginVersion}!."
            )
        }
    }
    
    private fun legacyInject(
        appExtension: AppExtension, project: Project, extension: ApmTraceExtension
    ) {
        
        project.afterEvaluate {
            
            if(!extension.isEnable) {
                return@afterEvaluate
            } // todo yuliu:  变体工件
            appExtension.applicationVariants.all {
                ApmTraceLegacyTransform.inject(extension, project, it)
            }
        }
    }
}
