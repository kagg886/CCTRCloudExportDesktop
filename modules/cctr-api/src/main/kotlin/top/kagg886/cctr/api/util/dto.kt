package top.kagg886.cctr.api.util

import io.ktor.util.logging.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

val json = Json {
    ignoreUnknownKeys = true
}

fun buildURLParams(vararg params: Pair<String, Any>): String {
    val s = buildString {
        for ((key, value) in params) {
            append(key).append("=").append(URLEncoder.encode(value.toString(),StandardCharsets.UTF_8)).append("&")
        }
    }
    return s.substring(0, s.length - 1)
}
@Serializable
data class BaseResponse(
    val code: Int,

    @SerialName("datas")
    val dataOrigin: JsonElement?,
) {
    inline fun <reified T> data(): T? {
        return kotlin.runCatching {
            json.decodeFromJsonElement<T>(dataOrigin!!)
        }.getOrElse {
            KtorSimpleLogger("dto").warn("convert data failed:",it)
            null
        }
    }
}

@Serializable
data class ErrorMessage(
    val error: String,
)