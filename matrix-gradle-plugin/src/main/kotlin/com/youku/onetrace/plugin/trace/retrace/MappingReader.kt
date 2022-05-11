package com.youku.onetrace.plugin.trace.retrace

import com.youku.onetrace.javalib.util.Log
import java.io.*


class MappingReader(private val proguardMappingFile: File) {
    /**
     * Reads the mapping file
     */
    @kotlin.jvm.Throws(IOException::class)
    fun read(mappingProcessor: MappingProcessor) {
        val reader = LineNumberReader(BufferedReader(FileReader(proguardMappingFile)))
        try {
            var className: String? = null // Read the class and class member mappings.
            while (true) {
                var line = reader.readLine() ?: break
                line = line.trim { it <= ' ' }
                if(!line.startsWith("#")) { // a class mapping
                    if(line.endsWith(SPLIT)) {
                        className = parseClassMapping(line, mappingProcessor)
                    } else className?.let { parseClassMemberMapping(it, line, mappingProcessor) }
                } else {
                    Log.i(TAG, "comment:# %s", line)
                }
            }
        } catch (err: IOException) {
            throw IOException("Can't read mapping file", err)
        } finally {
            try {
                reader.close()
            } catch (ex: IOException) { // do nothing
            }
        }
    }
    
    /**
     * 解析mapping.txt把类的混淆映射保存起来,返回类名
     * @param line read content
     * @param mappingProcessor
     * @return
     */
    private fun parseClassMapping(line: String, mappingProcessor: MappingProcessor): String? {
        val leftIndex = line.indexOf(ARROW)
        if(leftIndex < 0) {
            return null
        }
        val offset = 2
        val rightIndex = line.indexOf(SPLIT, leftIndex + offset)
        if(rightIndex < 0) {
            return null
        }
        
        // trim the elements.
        val className = line.substring(0, leftIndex).trim { it <= ' ' }
        val newClassName = line.substring(leftIndex + offset, rightIndex).trim { it <= ' ' }
        
        // Process this class name mapping.
        val ret = mappingProcessor.processClassMapping(className, newClassName)
        return if(ret) className else null
    }
    
    /**
     * Parses the a class member mapping
     * 解析mapping.txt下面的字段和方法
     *
     * @param className
     * @param line
     * @param mappingProcessor parse line such as
     * ___ ___ -> ___   字段混淆
     * ___:___:___ ___(___) -> ___
     * ___:___:___ ___(___):___ -> ___
     * ___:___:___ ___(___):___:___ -> ___
     */
    private fun parseClassMemberMapping(className: String, line: String, mappingProcessor: MappingProcessor) {
        var className = className
        val leftIndex1 = line.indexOf(SPLIT)
        val leftIndex2 = if(leftIndex1 < 0) -1 else line.indexOf(SPLIT, leftIndex1 + 1)
        val spaceIndex = line.indexOf(SPACE, leftIndex2 + 2)
        val argIndex1 = line.indexOf(LEFT_PUNC, spaceIndex + 1)
        val argIndex2 = if(argIndex1 < 0) -1 else line.indexOf(RIGHT_PUNC, argIndex1 + 1)
        val leftIndex3 = if(argIndex2 < 0) -1 else line.indexOf(SPLIT, argIndex2 + 1)
        val leftIndex4 = if(leftIndex3 < 0) -1 else line.indexOf(SPLIT, leftIndex3 + 1)
        val rightIndex = line.indexOf(ARROW, (if(leftIndex4 >= 0) leftIndex4 else if(leftIndex3 >= 0) leftIndex3 else if(argIndex2 >= 0) argIndex2 else spaceIndex) + 1)
        if(spaceIndex < 0 || rightIndex < 0) {
            return
        }
        
        // trim the elements.
        val type = line.substring(leftIndex2 + 1, spaceIndex).trim { it <= ' ' }
        var name = line.substring(spaceIndex + 1, if(argIndex1 >= 0) argIndex1 else rightIndex).trim { it <= ' ' }
        val newName = line.substring(rightIndex + 2).trim { it <= ' ' }
        val newClassName = className
        val dotIndex = name.lastIndexOf(DOT)
        if(dotIndex >= 0) {
            className = name.substring(0, dotIndex)
            name = name.substring(dotIndex + 1)
        }
        
        // parse class member mapping.
        if(type.length > 0 && name.length > 0 && newName.length > 0 && argIndex2 >= 0) {
            val arguments = line.substring(argIndex1 + 1, argIndex2).trim { it <= ' ' }
            mappingProcessor.processMethodMapping(className, type, name, arguments, newClassName, newName)
        }
    }
    
    companion object {
        private const val TAG = "MappingReader"
        private const val SPLIT = ":"
        private const val SPACE = " "
        private const val ARROW = "->"
        private const val LEFT_PUNC = "("
        private const val RIGHT_PUNC = ")"
        private const val DOT = "."
    }
}