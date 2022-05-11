package com.youku.onetrace.plugin.task

import com.android.build.gradle.AppExtension
import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.javalib.util.Util
import com.youku.onetrace.plugin.compat.CreationConfig
import com.youku.onetrace.plugin.compat.ApmTraceCompat
import com.youku.onetrace.plugin.extension.ApmRemoveUnusedResExtension
import com.youku.onetrace.plugin.trace.extension.ApmTraceExtension
import org.gradle.api.Project

class ApmTasksManager {
    
    companion object {
        const val TAG = "Apm.TasksManager"
    }
    
    fun createApmTasks(
        android: AppExtension, project: Project, traceExtension: ApmTraceExtension, removeUnusedResourcesExtension: ApmRemoveUnusedResExtension
    ) {
        
        createApmTraceTask(android, project, traceExtension)
        
        createRemoveUnusedResourcesTask(android, project, removeUnusedResourcesExtension)
    }
    
    
    // todo yuliu:  创建跟踪任务task
    private fun createApmTraceTask(
        android: AppExtension, project: Project, traceExtension: ApmTraceExtension
    ) {
        ApmTraceCompat().inject(android, project, traceExtension)
    }
    
    // todo yuliu:  创建移除无用资源task
    private fun createRemoveUnusedResourcesTask(
        android: AppExtension, project: Project, removeUnusedResourcesExtension: ApmRemoveUnusedResExtension
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
