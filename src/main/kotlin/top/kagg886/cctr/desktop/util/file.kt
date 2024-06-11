package top.kagg886.cctr.desktop.util

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
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


fun List<File>.convertToPDF(file: File): Boolean {
    return runCatching {
        PdfDocument(PdfWriter(file)).use {
            val document = Document(it)
            for (i in this) {
                val image = Image(ImageDataFactory.create(i.readBytes()))
                document.add(image)
            }
        }
        true
    }.getOrElse { false }
}
