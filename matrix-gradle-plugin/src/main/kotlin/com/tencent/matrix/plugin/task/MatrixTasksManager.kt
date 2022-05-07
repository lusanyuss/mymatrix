

package com.tencent.matrix.plugin.task

import com.android.build.gradle.AppExtension
import com.tencent.matrix.javalib.util.Log
import com.tencent.matrix.javalib.util.Util
import com.tencent.matrix.plugin.compat.CreationConfig
import com.tencent.matrix.plugin.compat.MatrixTraceCompat
import com.tencent.matrix.plugin.extension.MatrixRemoveUnusedResExtension
import com.tencent.matrix.trace.extension.MatrixTraceExtension
import org.gradle.api.Project

class MatrixTasksManager {

    companion object {
        const val TAG = "Matrix.TasksManager"
    }

    fun createMatrixTasks(android: AppExtension,
                          project: Project,
                          traceExtension: MatrixTraceExtension,
                          removeUnusedResourcesExtension: MatrixRemoveUnusedResExtension) {

        createMatrixTraceTask(android, project, traceExtension)

        createRemoveUnusedResourcesTask(android, project, removeUnusedResourcesExtension)
    }

    private fun createMatrixTraceTask(
            android: AppExtension,
            project: Project,
            traceExtension: MatrixTraceExtension) {
        MatrixTraceCompat().inject(android, project, traceExtension)
    }
    private fun createRemoveUnusedResourcesTask(
            android: AppExtension,
            project: Project,
            removeUnusedResourcesExtension: MatrixRemoveUnusedResExtension) {

        project.afterEvaluate {

            if (!removeUnusedResourcesExtension.enable) {
                return@afterEvaluate
            }

            android.applicationVariants.all { variant ->
                if (Util.isNullOrNil(removeUnusedResourcesExtension.variant) ||
                        variant.name.equals(removeUnusedResourcesExtension.variant, true)) {
                    Log.i(TAG, "RemoveUnusedResourcesExtension: %s", removeUnusedResourcesExtension)

                    val removeUnusedResourcesTaskProvider = if (removeUnusedResourcesExtension.v2) {
                        val action = RemoveUnusedResourcesTaskV2.CreationAction(
                                CreationConfig(variant, project), removeUnusedResourcesExtension
                        )
                        project.tasks.register(action.name, action.type, action)
                    } else {
                        val action = RemoveUnusedResourcesTask.CreationAction(
                                CreationConfig(variant, project), removeUnusedResourcesExtension
                        )
                        project.tasks.register(action.name, action.type, action)
                    }

                    variant.assembleProvider?.configure {
                        it.dependsOn(removeUnusedResourcesTaskProvider)
                    }

                    removeUnusedResourcesTaskProvider.configure {
                        it.dependsOn(variant.packageApplicationProvider)
                    }
                }
            }
        }
    }
}
