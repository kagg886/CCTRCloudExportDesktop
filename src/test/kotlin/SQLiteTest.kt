import kotlinx.coroutines.runBlocking
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.service.TaskService
import top.kagg886.cctr.backend.dao.Tasks
import java.time.LocalDateTime
import kotlin.test.Test

class SQLiteTest {
    @Test
    fun testSQL():Unit = runBlocking {
        repeat(50) {
            TaskService.insert(
                Task(
                    1,
                    "1",
                    "1",
                    "1",
                    Tasks.TaskStatus.PROCESSING,
                    Tasks.ExportType.PDF,
                    LocalDateTime.now(),
                    mapOf()
                )
            )
        }
    }
}