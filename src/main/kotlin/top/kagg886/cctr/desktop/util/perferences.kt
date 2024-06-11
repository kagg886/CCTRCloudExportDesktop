package top.kagg886.cctr.desktop.util

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.*

private data class Wrapper<T>(
    val t: T
) {
    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random().nextInt()
    }
}


class PreferenceManager<T : Any>(
    private val conf: String,
    content: T,
    private val serializer: KSerializer<T>,
) {
    private val flow = MutableStateFlow(Wrapper(content))

    suspend fun set(block: T.() -> Unit) {
        val current = flow.value.t
        block(current)
        val s = Json.encodeToString(serializer, current)
        root_config_file.resolve("$conf.json").apply {
            if (!exists()) {
                parentFile.mkdirs()
                createNewFile()
            }
        }.writeText(s)
        flow.emit(Wrapper(current))
    }

    suspend fun value() = flow.first().t

    suspend fun collect(value: suspend (T) -> Unit) {
        flow.collect {
            value(it.t)
        }
    }

    @Composable
    fun collectAsState(): State<T> {
        val state by flow.collectAsState()
        return remember(state) {
            mutableStateOf(state.t)
        }
    }

    fun <A> get(block: T.() -> A) = block(flow.value.t)


    override fun toString(): String = flow.value.toString()

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