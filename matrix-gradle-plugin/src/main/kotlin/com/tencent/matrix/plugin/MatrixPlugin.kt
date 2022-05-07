

package com.tencent.matrix.plugin

import com.android.build.gradle.AppExtension
import com.tencent.matrix.javalib.util.Log
import com.tencent.matrix.plugin.extension.MatrixExtension
import com.tencent.matrix.plugin.extension.MatrixRemoveUnusedResExtension
import com.tencent.matrix.plugin.task.MatrixTasksManager
import com.tencent.matrix.trace.extension.MatrixTraceExtension
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware

class MatrixPlugin : Plugin<Project> {
    companion object {
        const val TAG = "Matrix.Plugin"
    }

    override fun apply(project: Project) {

        val matrix = project.extensions.create("matrix", MatrixExtension::class.java)
        val traceExtension = (matrix as ExtensionAware).extensions.create("trace", MatrixTraceExtension::class.java)
        val removeUnusedResourcesExtension = matrix.extensions.create("removeUnusedResources", MatrixRemoveUnusedResExtension::class.java)

        if (!project.plugins.hasPlugin("com.android.application")) {
            throw GradleException("Matrix Plugin, Android Application plugin required.")
        }

        project.afterEvaluate {
            Log.setLogLevel(matrix.logLevel)
        }

        MatrixTasksManager().createMatrixTasks(
                project.extensions.getByName("android") as AppExtension,
                project,
                traceExtension,
                removeUnusedResourcesExtension
        )
    }
}
