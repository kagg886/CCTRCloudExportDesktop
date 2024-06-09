package top.kagg886.cctr.api.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.zip.GZIPInputStream
import javax.crypto.Cipher


object RSA {
    //source from https://www.cctrcloud.net/exam/lib/config.js
    private const val PUB_KEY =
        """MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDDLyh5Lb/5TprGvS9yFCcpCYnb0FuSyY3+TPbJI7Pv3+u4eFoqGGN46qyFOVLhUuFRttMfoA8h8yrdYCssLi93baoByTMYf5/KVlviLKXWd3TDOJdeSX4d+qLUp/WK0ckm2VaJuY5vW0x5x6WbZ8MSxwTDMqNNMgVUdOgD3MIScwIDAQAB"""

    private var cipher: Cipher = Cipher.getInstance("RSA")

    init {
        cipher.init(
            Cipher.ENCRYPT_MODE,
            KeyFactory.getInstance("RSA").generatePublic(
                X509EncodedKeySpec(Base64.getDecoder().decode(PUB_KEY))
            )
        )
    }

    fun encrypt(str: String): String = Base64.getEncoder().encodeToString(cipher.doFinal(str.toByteArray()))
}

fun ByteArray.decompress(): ByteArray {
    val bis = ByteArrayInputStream(Base64.getDecoder().decode(this))
    val gzipInputStream = GZIPInputStream(bis)
    val bos = ByteArrayOutputStream()
    val buffer = ByteArray(1024)
    var len: Int
    while ((gzipInputStream.read(buffer).also { len = it }) != -1) {
        bos.write(buffer, 0, len)
    }
    bos.close()
    gzipInputStream.close()
    return bos.toByteArray()
}