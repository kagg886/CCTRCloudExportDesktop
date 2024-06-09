import kotlinx.serialization.Serializable
import top.kagg886.cctr.desktop.util.PreferenceManager
import kotlin.test.Test

class PreferenceTest {

    @Test
    fun testPreference() {
        @Serializable
        data class A(var a: Int, var b: Int)

        val s = PreferenceManager.decodeFromFile("path") {
            A(1, 2)
        }
        println(s)
        s.set {
            a = 3
            b = 4
        }
        println(s)
    }
}