package top.kagg886.cctr.desktop.util

import kotlinx.serialization.Serializable
@Serializable
data class EdgeConfig(
    var version: String,
    var pool:Int = 3
)

val edge_config = PreferenceManager.decodeFromFile<EdgeConfig>("edge") {
    EdgeConfig("1.0.0")
}