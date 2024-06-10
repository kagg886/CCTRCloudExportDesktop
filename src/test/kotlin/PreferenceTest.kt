import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import top.kagg886.cctr.desktop.util.PreferenceManager
import java.util.Random
import kotlin.test.Test

class PreferenceTest {

    @Serializable
    data class A(var a:Int,var b:Int)
    private val s = PreferenceManager.decodeFromFile<A>("a") {
        A(1, 2)
    }

    @Test
    fun testCompose() {
        application {
            Window(onCloseRequest = ::exitApplication) {

                val k by s.collectAsState()
                Button(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        s.set {
                            a = Random().nextInt()
                            b = Random().nextInt()
                        }
                    }
                }) {
                    Text("$k")
                }
            }
        }
    }

    @Test
    fun testPreference() :Unit = runBlocking {
        @Serializable
        data class A(var a: Int, var b: Int)

        val s = PreferenceManager.decodeFromFile("path") {
            A(1, 2)
        }
        val job = launch(Dispatchers.IO) {
            s.collect {
                println(it)
            }
        }
        println("set 1")
        s.set {
            a = Random().nextInt()
            b = Random().nextInt()
        }
        delay(1000)
        println("set 2")
        s.set {
            a = Random().nextInt()
            b = Random().nextInt()
        }
        delay(1000)
        println("prepare close")
        job.cancel()
    }
}