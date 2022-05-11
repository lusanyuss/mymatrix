package com.youku.onetrace.plugin.extension

open class MatrixRemoveUnusedResExtension(
    
    var v2: Boolean = false,
    
    var enable: Boolean = false,
    var shrinkArsc: Boolean = false,
    var needSign: Boolean = false,
    var apksignerPath: String = "",
    var apkCheckerPath: String = "",
    var ignoreResources: Set<String> = HashSet(),
    
    var variant: String = "",
    
    // WIP. Should not use these options yet.
    var use7zip: Boolean = false,
    var zipAlign: Boolean = false,
    var shrinkDuplicates: Boolean = false,
    var embedResGuard: Boolean = false,
    var sevenZipPath: String = "",
    var zipAlignPath: String = "",
    
    // Deprecated
    var unusedResources: HashSet<String> = HashSet()
) {
    
    override fun toString(): String {
        return """|
           | enable = ${enable}
           | v2 = ${v2}
           | variant = ${variant}
           | needSign = ${needSign}
           | shrinkArsc = ${shrinkArsc}
           | shrinkDuplicates = ${shrinkDuplicates}
           | embedResGuard = ${embedResGuard}
           | apkCheckerPath = ${apkCheckerPath}
           | apkSignerPath = ${apksignerPath}
           | sevenZipPath = ${sevenZipPath}
           | zipAlignPath = ${zipAlignPath}
           | ignoreResources = ${ignoreResources}
        """.trimMargin()
    }
}
