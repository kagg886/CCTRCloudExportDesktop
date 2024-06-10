package top.kagg886.cctr.desktop.util

import java.io.File
import java.lang.UnsupportedOperationException


enum class Platform {
    WINDOWS, LINUX
}

val os by lazy {
    with(System.getProperty("os.name").lowercase()) {
        if (this.contains("windows")) {
            return@with Platform.WINDOWS
        }
        if (this.contains("linux")) {
            return@with Platform.LINUX
        }
        throw UnsupportedOperationException("$this is not supported")
    }
}

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