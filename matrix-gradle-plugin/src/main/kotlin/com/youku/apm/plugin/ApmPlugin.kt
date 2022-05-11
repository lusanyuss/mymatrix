package com.youku.apm.plugin

import com.android.build.gradle.AppExtension
import com.youku.apm.javalib.util.Log
import com.youku.apm.plugin.extension.ApmExtension
import com.youku.apm.plugin.extension.ApmRemoveUnusedResExtension
import com.youku.apm.plugin.task.ApmTasksManager
import com.youku.apm.plugin.trace.extension.ApmTraceExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

class ApmPlugin : Plugin<Project> {
    companion object {
        const val TAG = "Apm.Plugin"
    }
    
    override fun apply(project: Project) {
        
        val apm = project.extensions.create("apm", ApmExtension::class.java)
        val traceExtension = (apm as ExtensionAware).extensions.create("trace", ApmTraceExtension::class.java)
        val removeUnusedResourcesExtension = apm.extensions.create("removeUnusedResources", ApmRemoveUnusedResExtension::class.java)
        
        if(!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("Apm Plugin, Android Application plugin required.")
        }
        
        project.afterEvaluate {
            Log.setLogLevel(apm.logLevel)
        }
        
        ApmTasksManager().createApmTasks(
            project.extensions.getByName("android") as AppExtension, project, traceExtension, removeUnusedResourcesExtension
        )
    }
}