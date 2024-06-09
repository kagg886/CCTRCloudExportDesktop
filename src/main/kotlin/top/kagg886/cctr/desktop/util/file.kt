package top.kagg886.cctr.desktop.util

import java.io.File

val root_file = File("cctr-desktop").absoluteFile.apply {
    if (!exists()) {
        parentFile.mkdirs()
    }
}

val root_driver_file = root_file.resolve("driver").apply {
    if (!exists()) {
        parentFile.mkdirs()
    }
}

val root_config_file = root_file.resolve("config").apply {
    if (!exists()) {
        parentFile.mkdirs()
    }
}