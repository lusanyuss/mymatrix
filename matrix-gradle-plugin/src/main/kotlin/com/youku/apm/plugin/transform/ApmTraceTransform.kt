package com.youku.apm.plugin.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.builder.model.AndroidProject.FD_OUTPUTS
import com.android.utils.FileUtils
import com.google.common.base.Joiner
import com.youku.apm.javalib.util.Log
import com.youku.apm.plugin.trace.ApmTrace
import com.youku.apm.plugin.trace.Configuration
import com.youku.apm.plugin.trace.extension.ApmTraceExtension
import org.gradle.api.Project
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ApmTraceTransform(
    private val project: Project, private val extension: ApmTraceExtension, private var transparent: Boolean = true
) : Transform() {
    
    companion object {
        const val TAG = "Apm.TraceTransform"
    }
    
    fun enable() {
        transparent = false
    }
    
    fun disable() {
        transparent = true
    }
    
    override fun getName(): String {
        return "ApmTraceTransform"
    }
    
    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }
    
    override fun getScopes(): MutableSet<in QualifiedContent.Scope>? {
        return TransformManager.SCOPE_FULL_PROJECT
    }
    
    override fun isIncremental(): Boolean {
        return true
    }
    
    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        
        if(transparent) {
            transparent(transformInvocation)
        } else {
            transforming(transformInvocation)
        }
    }
    
    /**
     * Passes all inputs throughout.
     * TODO: How to avoid this trivial work?
     */
    private fun transparent(invocation: TransformInvocation) {
        
        val outputProvider = invocation.outputProvider!!
        
        if(!invocation.isIncremental) {
            outputProvider.deleteAll()
        }
        
        for (ti in invocation.inputs) {
            for (jarInput in ti.jarInputs) {
                val inputJar = jarInput.file
                val outputJar = outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                
                if(invocation.isIncremental) {
                    when (jarInput.status) {
                        Status.NOTCHANGED -> {
                        }
                        Status.ADDED, Status.CHANGED -> {
                            copyFileAndMkdirsAsNeed(inputJar, outputJar)
                        }
                        Status.REMOVED -> FileUtils.delete(outputJar)
                        else -> {}
                    }
                } else {
                    copyFileAndMkdirsAsNeed(inputJar, outputJar)
                }
            }
            for (directoryInput in ti.directoryInputs) {
                val inputDir = directoryInput.file
                val outputDir = outputProvider.getContentLocation(
                    directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
                )
                
                if(invocation.isIncremental) {
                    for (entry in directoryInput.changedFiles.entries) {
                        val inputFile = entry.key
                        when (entry.value) {
                            Status.NOTCHANGED -> {
                            }
                            Status.ADDED, Status.CHANGED -> if(!inputFile.isDirectory) {
                                val outputFile = toOutputFile(outputDir, inputDir, inputFile)
                                copyFileAndMkdirsAsNeed(inputFile, outputFile)
                            }
                            Status.REMOVED -> {
                                val outputFile = toOutputFile(outputDir, inputDir, inputFile)
                                FileUtils.deleteIfExists(outputFile)
                            }
                            else -> {}
                        }
                    }
                } else {
                    for (`in` in FileUtils.getAllFiles(inputDir)) {
                        val out = toOutputFile(outputDir, inputDir, `in`)
                        copyFileAndMkdirsAsNeed(`in`, out)
                    }
                }
            }
        }
    }
    
    private fun copyFileAndMkdirsAsNeed(from: File, to: File) {
        if(from.exists()) {
            to.parentFile.mkdirs()
            FileUtils.copyFile(from, to)
        }
    }
    
    private fun toOutputFile(outputDir: File, inputDir: File, inputFile: File): File {
        return File(outputDir, FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir))
    }
    
    private fun configure(transformInvocation: TransformInvocation): Configuration {
        
        val buildDir = project.buildDir.absolutePath
        val dirName = transformInvocation.context.variantName
        
        val mappingOut = Joiner.on(File.separatorChar).join(
            buildDir, FD_OUTPUTS, "mapping", dirName
        )
        
        return Configuration.Builder().setBaseMethodMap(extension.baseMethodMapFile).setBlockListFile(extension.blackListFile).setMethodMapFilePath("$mappingOut/methodMapping.txt")
            .setIgnoreMethodMapFilePath("$mappingOut/ignoreMethodMapping.txt").setMappingPath(mappingOut).setSkipCheckClass(extension.skipCheckClass).build()
    }
    
    private fun transforming(invocation: TransformInvocation) {
        
        val start = System.currentTimeMillis()
        
        val outputProvider = invocation.outputProvider!!
        val isIncremental = invocation.isIncremental && this.isIncremental
        
        if(!isIncremental) {
            outputProvider.deleteAll()
        }
        
        val config = configure(invocation)
        
        val changedFiles = ConcurrentHashMap<File, Status>()
        val inputToOutput = ConcurrentHashMap<File, File>()
        val inputFiles = ArrayList<File>()
        
        var transformDirectory: File? = null
        
        for (input in invocation.inputs) {
            for (directoryInput in input.directoryInputs) {
                changedFiles.putAll(directoryInput.changedFiles)
                val inputDir = directoryInput.file
                inputFiles.add(inputDir)
                val outputDirectory = outputProvider.getContentLocation(
                    directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
                )
                
                inputToOutput[inputDir] = outputDirectory
                if(transformDirectory == null) transformDirectory = outputDirectory.parentFile
            }
            
            for (jarInput in input.jarInputs) {
                val inputFile = jarInput.file
                changedFiles[inputFile] = jarInput.status
                inputFiles.add(inputFile)
                val outputJar = outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                
                inputToOutput[inputFile] = outputJar
                if(transformDirectory == null) transformDirectory = outputJar.parentFile
            }
        }
        
        if(inputFiles.size == 0 || transformDirectory == null) {
            Log.i(TAG, "Apm trace do not find any input files")
            return
        }
        
        // Get transform root dir.
        val outputDirectory = transformDirectory
        
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
            traceClassDirectoryOutput = outputDirectory,
            inputToOutput = inputToOutput,
            legacyReplaceChangedFile = null,
            legacyReplaceFile = null,
            uniqueOutputName = true
        )
        
        val cost = System.currentTimeMillis() - start
        Log.i(TAG, " Insert apm trace instrumentations cost time: %sms.", cost)
    }
    
}