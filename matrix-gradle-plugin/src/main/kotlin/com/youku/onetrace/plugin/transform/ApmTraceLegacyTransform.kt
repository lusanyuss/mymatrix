package com.youku.onetrace.plugin.transform

import com.android.build.api.transform.*
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.builder.model.AndroidProject.FD_OUTPUTS
import com.google.common.base.Joiner
import com.youku.onetrace.javalib.util.Log
import com.youku.onetrace.javalib.util.ReflectUtil
import com.youku.onetrace.javalib.util.Util
import com.youku.onetrace.plugin.trace.ApmTrace
import com.youku.onetrace.plugin.trace.Configuration
import com.youku.onetrace.plugin.trace.extension.ApmTraceExtension
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.ConcurrentHashMap

// For Android Gradle Plugin 3.5.0
class ApmTraceLegacyTransform(
    private val project: Project, private val config: Configuration, private val origTransform: Transform
) : Transform() {
    
    companion object {
        const val TAG = "Apm.TraceLegacyTransform"
        
        fun inject(extension: ApmTraceExtension, project: Project, variant: BaseVariant) {
            
            val mappingOut = Joiner.on(File.separatorChar).join(
                project.buildDir.absolutePath, FD_OUTPUTS, "mapping", variant.dirName
            )
            
            val traceClassOut = Joiner.on(File.separatorChar).join(
                project.buildDir.absolutePath, FD_OUTPUTS, "traceClassOut", variant.dirName
            )
            
            val config = Configuration.Builder().setPackageName(variant.applicationId).setBaseMethodMap(extension.baseMethodMapFile).setBlockListFile(extension.blackListFile)
                .setMethodMapFilePath("$mappingOut/methodMapping.txt").setIgnoreMethodMapFilePath("$mappingOut/ignoreMethodMapping.txt").setMappingPath(mappingOut).setTraceClassOut(traceClassOut)
                .setSkipCheckClass(extension.isSkipCheckClass).build()
            
            val hardTask = getTransformTaskName(extension.customDexTransformName, variant.name)
            for (task in project.tasks) {
                for (str in hardTask) {
                    if(task.name.equals(str, ignoreCase = true) && task is TransformTask) {
                        Log.i(TAG, "successfully inject task:" + task.name)
                        val field = TransformTask::class.java.getDeclaredField("transform")
                        field.isAccessible = true
                        field.set(task, ApmTraceLegacyTransform(project, config, task.transform))
                        break
                    }
                }
            }
        }
        
        private fun getTransformTaskName(customDexTransformName: String?, buildTypeSuffix: String): Array<String> {
            return if(!Util.isNullOrNil(customDexTransformName)) {
                arrayOf(
                    "${customDexTransformName}For$buildTypeSuffix"
                )
            } else {
                arrayOf(
                    "transformClassesWithDexBuilderFor$buildTypeSuffix", "transformClassesWithDexFor$buildTypeSuffix"
                )
            }
        }
    }
    
    override fun getName(): String {
        return TAG
    }
    
    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }
    
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }
    
    override fun isIncremental(): Boolean {
        return true
    }
    
    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation);
        val start = System.currentTimeMillis();
        try {
            doTransform(transformInvocation); // hack
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        val cost = System.currentTimeMillis() - start;
        val begin = System.currentTimeMillis();
        origTransform.transform(transformInvocation);
        val origTransformCost = System.currentTimeMillis() - begin;
        Log.i(
            "Apm.$name", "[transform] cost time: %dms %s:%sms ApmTraceTransform:%sms", System.currentTimeMillis() - start, origTransform.javaClass.simpleName, origTransformCost, cost
        );
    }
    
    private fun doTransform(invocation: TransformInvocation) {
        
        val start = System.currentTimeMillis()
        
        val isIncremental = invocation.isIncremental && this.isIncremental
        
        val changedFiles = ConcurrentHashMap<File, Status>()
        val inputFiles = ArrayList<File>()
        
        val fileToInput = ConcurrentHashMap<File, QualifiedContent>()
        
        for (input in invocation.inputs) {
            for (directoryInput in input.directoryInputs) {
                changedFiles.putAll(directoryInput.changedFiles)
                inputFiles.add(directoryInput.file)
                fileToInput[directoryInput.file] = directoryInput
            }
            
            for (jarInput in input.jarInputs) {
                changedFiles[jarInput.file] = jarInput.status
                inputFiles.add(jarInput.file)
                fileToInput[jarInput.file] = jarInput
            }
        }
        
        if(inputFiles.size == 0) {
            Log.i(TAG, "Apm trace do not find any input files")
            return
        }
        
        val legacyReplaceChangedFile = { inputDir: File, map: Map<File, Status> ->
            replaceChangedFile(fileToInput[inputDir] as DirectoryInput, map)
            inputDir as Object
        }
        
        var legacyReplaceFile = { input: File, output: File ->
            replaceFile(fileToInput[input] as QualifiedContent, output)
            input as Object
        }
        
        ApmTrace(
            ignoreMethodMapFilePath = config.ignoreMethodMapFilePath,
            methodMapFilePath = config.methodMapFilePath,
            baseMethodMapPath = config.baseMethodMapPath,
            blockListFilePath = config.blockListFilePath,
            mappingDir = config.mappingDir,
            project = project
        ).doTransform(
            classInputs = inputFiles,
            changedFiles = changedFiles,
            isIncremental = isIncremental,
            skipCheckClass = config.skipCheckClass,
            traceClassDirectoryOutput = File(config.traceClassOut),
            inputToOutput = ConcurrentHashMap(),
            legacyReplaceChangedFile = legacyReplaceChangedFile,
            legacyReplaceFile = legacyReplaceFile,
            uniqueOutputName = true
        )
        
        val cost = System.currentTimeMillis() - start
        Log.i(TAG, " Insert apm trace instrumentations cost time: %sms.", cost)
    }
    
    private fun replaceFile(input: QualifiedContent, newFile: File) {
        val fileField = ReflectUtil.getDeclaredFieldRecursive(input.javaClass, "file");
        fileField.set(input, newFile);
    }
    
    private fun replaceChangedFile(dirInput: DirectoryInput, changedFiles: Map<File, Status>) {
        val changedFilesField = ReflectUtil.getDeclaredFieldRecursive(dirInput.javaClass, "changedFiles")
        changedFilesField.set(dirInput, changedFiles)
    }
    
}
