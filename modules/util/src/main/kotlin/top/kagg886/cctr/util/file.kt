package top.kagg886.cctr.util
import java.io.*
import java.util.zip.*
import kotlin.io.path.createTempDirectory


fun useTempDictionary(block: (File) -> Unit) {
    val f = createTempDirectory("cctr").toFile()
    block(f)
    f.deleteRecursively()
}

suspend fun useTempDictionarySuspend(block: suspend (File) -> Unit) {
    val f = createTempDirectory("cctr").toFile()
    block(f)
    f.deleteRecursively()
}

fun useTempFile(suffix:String = "",block: (File) -> Unit) {
    val f = kotlin.io.path.createTempFile("cctr",suffix = suffix).toFile()
    block(f)
    f.delete()
}

suspend fun useTempFileSuspend(suffix:String = "",block: suspend (File) -> Unit) {
    val f = kotlin.io.path.createTempFile("cctr",suffix = suffix).toFile()
    block(f)
    f.delete()
}

fun File.zip(out: File = File(this.parent, this.nameWithoutExtension + ".zip")) {
    ZipOutputStream(BufferedOutputStream(FileOutputStream(out))).use {
        compressFile(this, "", it)
    }
}

private fun compressFile(file: File, prefix: String, zipOut: ZipOutputStream) {
    if (file.isDirectory) {
        file.listFiles()?.forEach { child ->
            compressFile(child, "$prefix${child.name}/", zipOut)
        }
    } else {
        val entryName = prefix.substring(0, prefix.length - 1)
        val zipEntry = ZipEntry(entryName)
        zipOut.putNextEntry(zipEntry)

        val buffer = ByteArray(1024)
        val inputStream = FileInputStream(file)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            zipOut.write(buffer, 0, length)
        }

        inputStream.close()
        zipOut.closeEntry()
    }
}

fun File.unzip(out: File = File(this.parent, this.nameWithoutExtension)) {
    if (!this.exists() || this.isDirectory) {
        throw FileNotFoundException(this.absolutePath)
    }
    val zipInputStream = ZipInputStream(CheckedInputStream(FileInputStream(this), Adler32()))
    var zipEntry: ZipEntry? = null

    if (!out.exists()) {
        out.mkdirs()
    }
    val bufferedInputStream = BufferedInputStream(zipInputStream)
    while ((zipInputStream.nextEntry?.also { zipEntry = it }) != null) {
        val file = out.absoluteFile.resolve(zipEntry!!.name)
        if (zipEntry!!.isDirectory) {
            file.mkdirs()
            continue
        }
        file.parentFile.mkdirs()
        file.createNewFile()
        val fileOutputStream = FileOutputStream(file)
        var x: Int
        val bytes = ByteArray(1024)
        while ((bufferedInputStream.read(bytes).also { x = it }) != -1) {
            fileOutputStream.write(bytes, 0, x)
        }
        fileOutputStream.close()
    }
    zipInputStream.close()
}