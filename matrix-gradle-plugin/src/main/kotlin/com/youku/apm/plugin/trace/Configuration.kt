package com.youku.apm.plugin.trace

import com.youku.apm.javalib.util.FileUtil
import com.youku.apm.javalib.util.Util
import com.youku.apm.plugin.trace.retrace.MappingCollector

class Configuration {
    var packageName: String? = null
    var mappingDir: String? = null
    var baseMethodMapPath: String? = null
    var methodMapFilePath: String? = null
    var ignoreMethodMapFilePath: String? = null
    var blockListFilePath: String? = null
    var traceClassOut: String? = null
    var skipCheckClass = false
    var blockSet = HashSet<String?>()
    
    constructor() {}
    internal constructor(
        packageName: String?,
        mappingDir: String?,
        baseMethodMapPath: String?,
        methodMapFilePath: String?,
        ignoreMethodMapFilePath: String?,
        blockListFilePath: String?,
        traceClassOut: String?,
        skipCheckClass: Boolean
    ) {
        this.packageName = packageName
        this.mappingDir = Util.nullAsNil(mappingDir)
        this.baseMethodMapPath = Util.nullAsNil(baseMethodMapPath)
        this.methodMapFilePath = Util.nullAsNil(methodMapFilePath)
        this.ignoreMethodMapFilePath = Util.nullAsNil(ignoreMethodMapFilePath)
        this.blockListFilePath = Util.nullAsNil(blockListFilePath)
        this.traceClassOut = Util.nullAsNil(traceClassOut)
        this.skipCheckClass = skipCheckClass
    }
    
    /**
     * 黑名单解析
     * @param processor
     * @return
     */
    fun parseBlockFile(processor: MappingCollector): Int {
        val blockStr = (TraceBuildConstants.DEFAULT_BLOCK_TRACE + FileUtil.readFileAsString(blockListFilePath))
        val blockArray = blockStr.trim { it <= ' ' }.replace("/", ".").replace("\r", "").split("\n").toTypedArray()
        if(blockArray != null) {
            for (block in blockArray) {
                if(block.length == 0) {
                    continue
                }
                if(block.startsWith("#")) {
                    continue
                }
                if(block.startsWith("[")) {
                    continue
                }
                if(block.startsWith("-keepclass ")) {
                    val jblock = block.replace("-keepclass ", "")
                    blockSet.add(processor.proguardClassName(jblock, jblock))
                } else if(block.startsWith("-keeppackage ")) {
                    val jblock = block.replace("-keeppackage ", "")
                    blockSet.add(processor.proguardPackageName(jblock, jblock))
                }
            }
        }
        return blockSet.size
    }
    
    override fun toString(): String {
        return """
            
            # Configuration
            |* packageName:	$packageName
            |* mappingDir:	$mappingDir
            |* baseMethodMapPath:	$baseMethodMapPath
            |* methodMapFilePath:	$methodMapFilePath
            |* ignoreMethodMapFilePath:	$ignoreMethodMapFilePath
            |* blockListFilePath:	$blockListFilePath
            |* traceClassOut:	$traceClassOut
            
            """.trimIndent()
    }
    
    class Builder {
        var packageName: String? = null
        var mappingPath: String? = null
        var baseMethodMap: String? = null
        var methodMapFile: String? = null
        var ignoreMethodMapFile: String? = null
        var blockListFile: String? = null
        var traceClassOut: String? = null
        var skipCheckClass = false
        fun setPackageName(packageName: String?): Builder {
            this.packageName = packageName
            return this
        }
        
        fun setMappingPath(mappingPath: String?): Builder {
            this.mappingPath = mappingPath
            return this
        }
        
        fun setBaseMethodMap(baseMethodMap: String?): Builder {
            this.baseMethodMap = baseMethodMap
            return this
        }
        
        fun setTraceClassOut(traceClassOut: String?): Builder {
            this.traceClassOut = traceClassOut
            return this
        }
        
        fun setMethodMapFilePath(methodMapDir: String?): Builder {
            methodMapFile = methodMapDir
            return this
        }
        
        fun setIgnoreMethodMapFilePath(methodMapDir: String?): Builder {
            ignoreMethodMapFile = methodMapDir
            return this
        }
        
        fun setBlockListFile(blockListFile: String?): Builder {
            this.blockListFile = blockListFile
            return this
        }
        
        fun setSkipCheckClass(skipCheckClass: Boolean): Builder {
            this.skipCheckClass = skipCheckClass
            return this
        }
        
        fun build(): Configuration {
            return Configuration(packageName, mappingPath, baseMethodMap, methodMapFile, ignoreMethodMapFile, blockListFile, traceClassOut, skipCheckClass)
        }
    }
}