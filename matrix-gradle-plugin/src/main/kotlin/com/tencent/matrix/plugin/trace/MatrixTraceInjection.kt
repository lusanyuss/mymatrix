

package com.tencent.matrix.plugin.trace

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.tasks.DexArchiveBuilderTask
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.builder.model.CodeShrinker
import com.tencent.matrix.javalib.util.Log
import com.tencent.matrix.plugin.compat.CreationConfig
import com.tencent.matrix.plugin.compat.CreationConfig.Companion.getCodeShrinker
import com.tencent.matrix.plugin.task.BaseCreationAction
import com.tencent.matrix.plugin.task.MatrixTraceTask
import com.tencent.matrix.plugin.transform.MatrixTraceTransform
import com.tencent.matrix.trace.extension.ITraceSwitchListener
import com.tencent.matrix.trace.extension.MatrixTraceExtension
import org.gradle.api.Project
import org.gradle.api.Task

class MatrixTraceInjection : ITraceSwitchListener {

    companion object {
        const val TAG = "Matrix.TraceInjection"
    }

    private var traceEnable = false

    override fun onTraceEnabled(enable: Boolean) {
        traceEnable = enable
    }

    fun inject(appExtension: AppExtension,
               project: Project,
               extension: MatrixTraceExtension) {
        injectTransparentTransform(appExtension, project, extension)
        project.afterEvaluate {
            if (extension.isEnable) {
                doInjection(appExtension, project, extension)
            }
        }
    }

    private var transparentTransform: MatrixTraceTransform? = null

    private fun injectTransparentTransform(appExtension: AppExtension,
                                           project: Project,
                                           extension: MatrixTraceExtension) {

        transparentTransform = MatrixTraceTransform(project, extension)
        appExtension.registerTransform(transparentTransform!!)
    }
    
    private fun doInjection(appExtension: AppExtension,
                            project: Project,
                            extension: MatrixTraceExtension) {
        appExtension.applicationVariants.all { variant ->
            if (injectTaskOrTransform(project, extension, variant) == InjectionMode.TransformInjection) {
                // Inject transform
                transformInjection()
            } else {
                // Inject task
                taskInjection(project, extension, variant)
            }
        }
    }

    private fun taskInjection(project: Project,
                              extension: MatrixTraceExtension,
                              variant: BaseVariant) {

        Log.i(TAG, "Using trace task mode.")

        project.afterEvaluate {

            val creationConfig = CreationConfig(variant, project)
            val action = MatrixTraceTask.CreationAction(creationConfig, extension)
            val traceTaskProvider = project.tasks.register(action.name, action.type, action)

            val variantName = variant.name

            val minifyTasks = arrayOf(
                    BaseCreationAction.computeTaskName("minify", variantName, "WithProguard")
            )

            var minify = false
            for (taskName in minifyTasks) {
                val taskProvider = BaseCreationAction.findNamedTask(project.tasks, taskName)
                if (taskProvider != null) {
                    minify = true
                    traceTaskProvider.dependsOn(taskProvider)
                }
            }

            if (minify) {
                val dexBuilderTaskName = BaseCreationAction.computeTaskName("dexBuilder", variantName, "")
                val taskProvider = BaseCreationAction.findNamedTask(project.tasks, dexBuilderTaskName)

                taskProvider?.configure { task: Task ->
                    traceTaskProvider.get().wired(creationConfig, task as DexArchiveBuilderTask)
                }

                if (taskProvider == null) {
                    Log.e(TAG, "Do not find '$dexBuilderTaskName' task. Inject matrix trace task failed.")
                }
            }
        }
    }

    private fun transformInjection() {

        Log.i(TAG, "Using trace transform mode.")

        transparentTransform!!.enable()
    }

    enum class InjectionMode {
        TaskInjection,
        TransformInjection,
    }

    private fun injectTaskOrTransform(project: Project,
                                      extension: MatrixTraceExtension,
                                      variant: BaseVariant): InjectionMode {

        if (!variant.buildType.isMinifyEnabled
                || extension.isTransformInjectionForced
                || getCodeShrinker(project) == CodeShrinker.R8) {
            return InjectionMode.TransformInjection
        }

        return InjectionMode.TaskInjection
    }


}
