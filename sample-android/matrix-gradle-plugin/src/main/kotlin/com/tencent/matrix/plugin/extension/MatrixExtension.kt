

package com.tencent.matrix.plugin.extension



open class MatrixExtension(
        var clientVersion: String = "",
        var uuid: String = "",
        var output: String = "",
        var logLevel: String = "I"
) {

    override fun toString(): String {
        return """| log vevel = $logLevel
//                  | uuid = $uuid
                """.trimMargin()
    }
}