package top.kagg886.cctr.desktop.util

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

data class Wrapper<T>(
    val t: T
)


class PreferenceManager<T : Any>(
    private val conf: String,
    content: T,
    private val serializer: KSerializer<T>,
) {
    private var t: T = content

    private val flow = MutableStateFlow(Wrapper(t))

    fun set(block: T.() -> Unit) {
        t.block()
        val s = Json.encodeToString(serializer, t)
        root_config_file.resolve("$conf.json").apply {
            if (!exists()) {
                parentFile.mkdirs()
                createNewFile()
            }
        }.writeText(s)
        flow.value = Wrapper(t)
    }

    suspend fun watch(value: suspend (T?) -> Unit) {
        flow.collectLatest {
            value(it.t)
        }
    }

    @Composable
    fun watchAsState(): State<T> {
        val state by flow.collectAsState()
        return remember(state) {
            mutableStateOf(state.t)
        }
    }

    fun <A> get(block: T.() -> A) = block(t)


    override fun toString(): String = t.toString()

    companion object {
        @OptIn(InternalSerializationApi::class)
        inline fun <reified T : Any> decodeFromFile(path: String, initial: () -> T): PreferenceManager<T> {
            val file = root_config_file.resolve("$path.json")
            if (file.exists()) {

                val decode = Json.decodeFromString<T>(file.readText())
                return PreferenceManager(path, decode, T::class.serializer())
            }

            return PreferenceManager(path, initial(), T::class.serializer())
        }
    }
}