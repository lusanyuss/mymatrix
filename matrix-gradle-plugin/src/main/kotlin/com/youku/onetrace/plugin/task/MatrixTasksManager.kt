package com.youku.onetrace.plugin.task

import com.android.build.gradle.AppExtension
import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.javalib.util.Util
import com.youku.onetrace.plugin.compat.CreationConfig
import com.youku.onetrace.plugin.compat.MatrixTraceCompat
import com.youku.onetrace.plugin.extension.MatrixRemoveUnusedResExtension
import com.youku.onetrace.plugin.trace.extension.MatrixTraceExtension
import org.gradle.api.Project

class MatrixTasksManager {
    
    companion object {
        const val TAG = "Matrix.TasksManager"
    }
    
    fun createMatrixTasks(
        android: AppExtension, project: Project, traceExtension: MatrixTraceExtension, removeUnusedResourcesExtension: MatrixRemoveUnusedResExtension
    ) {
        
        createMatrixTraceTask(android, project, traceExtension)
        
        createRemoveUnusedResourcesTask(android, project, removeUnusedResourcesExtension)
    }
    
    
    // todo yuliu:  创建跟踪任务task
    private fun createMatrixTraceTask(
        android: AppExtension, project: Project, traceExtension: MatrixTraceExtension
    ) {
        MatrixTraceCompat().inject(android, project, traceExtension)
    }
    
    // todo yuliu:  创建移除无用资源task
    private fun createRemoveUnusedResourcesTask(
        android: AppExtension, project: Project, removeUnusedResourcesExtension: MatrixRemoveUnusedResExtension
    ) {
        
        project.afterEvaluate {
            
            if(!removeUnusedResourcesExtension.enable) {
                return@afterEvaluate
            }
            
            android.applicationVariants.all { variant ->
                if(Util.isNullOrNil(removeUnusedResourcesExtension.variant) || variant.name.equals(removeUnusedResourcesExtension.variant, true)) {
                    Log.i(TAG, "RemoveUnusedResourcesExtension: %s", removeUnusedResourcesExtension)
                    
                    val removeUnusedResourcesTaskProvider = if(removeUnusedResourcesExtension.v2) {
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
