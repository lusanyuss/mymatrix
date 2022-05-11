package com.youku.onetrace.plugin.trace

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.google.common.collect.ImmutableList
import org.gradle.api.Project
import java.io.File
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader


object TraceClassLoader {
    @kotlin.jvm.Throws(MalformedURLException::class)
    fun getClassLoader(project: Project, inputFiles: Collection<File>): URLClassLoader {
        val urls = ImmutableList.Builder<URL>()
        val androidJar = getAndroidJar(project)
        if(androidJar != null) {
            urls.add(androidJar.toURI().toURL())
        }
        for (inputFile in inputFiles) {
            urls.add(inputFile.toURI().toURL())
        }
        val urlImmutableList = urls.build()
        val classLoaderUrls = urlImmutableList.toTypedArray()
        return URLClassLoader(classLoaderUrls)
    }
    
    private fun getAndroidJar(project: Project): File? {
        var extension: BaseExtension? = null
        if(project.plugins.hasPlugin("com.android.application")) {
            extension = project.extensions.findByType(AppExtension::class.java)
        } else if(project.plugins.hasPlugin("com.android.library")) {
            extension = project.extensions.findByType(LibraryExtension::class.java)
        }
        if(extension == null) {
            return null
        }
        var sdkDirectory = extension.sdkDirectory.absolutePath
        val compileSdkVersion = extension.compileSdkVersion
        sdkDirectory = sdkDirectory + File.separator + "platforms" + File.separator
        val androidJarPath = sdkDirectory + compileSdkVersion + File.separator + "android.jar"
        val androidJar = File(androidJarPath)
        return if(androidJar.exists()) androidJar else null
    }
}