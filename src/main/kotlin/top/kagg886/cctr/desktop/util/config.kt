package top.kagg886.cctr.desktop.util

import kotlinx.serialization.Serializable
import top.kagg886.cctr.driver.execWithArgs
import java.io.File

@Serializable
data class EdgeConfig(
    var version: String,
    var binary: String,
    var pool: Int = 3
)

val edge_config = PreferenceManager.decodeFromFile<EdgeConfig>("edge") {
    EdgeConfig("1.0.0", findEdgeBinary())
}

private fun findEdgeBinary() = when (os) {
    Platform.WINDOWS -> listOf(
        "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
        "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe"
    ).first { File(it).exists() }

    Platform.LINUX -> listOf(
        arrayOf("which", "microsoft-edge-stable"),
        arrayOf("which", "microsoft-edge-beta"),
        arrayOf("which", "microsoft-edge-dev"),
        arrayOf("which", "microsoft-edge")
    ).firstNotNullOfOrNull { runCatching { execWithArgs(it).trim() }.getOrNull() } ?: "unknown"
}

@Serializable
data class CCTRConfig(
    var schoolId:String,
    var userName:String,
    var password:String,
)

@Serializable
val cctr_last_modifier = PreferenceManager.decodeFromFile("cctr_last_modifier") {
    CCTRConfig("","","")
}