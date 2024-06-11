package top.kagg886.cctr.desktop.util

import ch.qos.logback.core.FileAppender
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class DateFileAppender<E> : FileAppender<E>() {
    override fun setFile(str: String) {
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())

        val base = root_file.resolve("log")

        var file = base.resolve("${date}.log")
        if (!file.parentFile.isDirectory) {
            file.parentFile.mkdirs()
        }
        var i = 1
        while (file.exists()) {
            file = base.resolve("${date}-$i.log")
            i++
        }
        file.createNewFile()
        super.setFile(file.absolutePath)
    }
}
class LatestFileAppender<E> : FileAppender<E>() {
    override fun setFile(base: String) {
        super.setFile(root_file.resolve("log").resolve("latest.log").absolutePath)
    }
}