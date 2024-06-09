package top.kagg886.cctr.desktop.util

import ch.qos.logback.core.FileAppender
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class CustomFileAppender<E> : FileAppender<E>() {

    private lateinit var file: File

    override fun setFile(base: String) {
        val date = SimpleDateFormat("yyyy-MM-dd").format(Date())

        file = Paths.get(base,"${date}.log").toFile()

        if (!file.parentFile.isDirectory) {
            file.parentFile.mkdirs()
        }

        var i = 1
        while (file.exists()) {
            file = Paths.get(base,"${date}-$i.log").toFile()
            i++
        }
        file.createNewFile()

        super.setFile(file.absolutePath)
    }
}