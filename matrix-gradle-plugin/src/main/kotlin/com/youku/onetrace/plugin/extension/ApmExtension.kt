package com.youku.onetrace.plugin.extension

/**
 * Created by caichongyang on 2017/6/20.
 */

open class ApmExtension(
    var clientVersion: String = "", var uuid: String = "", var output: String = "", var logLevel: String = "I"
) {
    
    override fun toString(): String {
        return """| log vevel = $logLevel
//                  | uuid = $uuid
                """.trimMargin()
    }
}