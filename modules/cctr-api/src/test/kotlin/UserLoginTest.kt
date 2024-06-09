import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.util.CCTRException
import java.io.File
import kotlin.test.Test

class UserLoginTest {

    @Test
    fun testUser() {
        assertThrows<CCTRException> {
            runBlocking {
                CCTRUser.newCCTRUser {
                    schoolId = "U10"
                    username = ""
                    password = ""
                }
            }
        }
        assertThrows<CCTRException> {
            runBlocking {
                CCTRUser.newCCTRUser {
                    schoolId = "U101441"
                    username = "2203050528"
                    password = "123456"
                }
            }
        }
    }

    @Test
    fun testLogin() {
        assertDoesNotThrow {
            runBlocking {
                CCTRUser.newCCTRUser {
                    schoolId = "U101441"
                    username = "2203050528"
                    password = File("password.txt").readText()
                }
            }
        }
    }
}