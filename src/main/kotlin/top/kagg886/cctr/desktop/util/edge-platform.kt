package top.kagg886.cctr.desktop.util

import java.io.File


enum class Platform {
    WINDOWS, LINUX
}

val os = Platform.valueOf(System.getProperty("os.name").uppercase())

fun getEdgeDownloadURL(version: String): String {
    val platformStr = when (os) {
        Platform.WINDOWS -> "win64"
        Platform.LINUX -> "linux64"
    }
    return "https://msedgedriver.azureedge.net/$version/edgedriver_$platformStr.zip"
}

val msedgedriverName by lazy {
    "msedgedriver".plus(if (os == Platform.WINDOWS) ".exe" else "")
}

fun getEdgeDriverFile(version: String): File = root_driver_file.resolve("${os}_${version}")

fun checkEdgeDriverExists(version: String): Boolean = getEdgeDriverFile(version).exists()