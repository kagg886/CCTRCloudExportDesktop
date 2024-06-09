package top.kagg886.cctr.backend.dao

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime


object Tasks : IntIdTable("task") {
    val schoolId = varchar("school_id", 20)
    val username = varchar("username", 20)
    val password = varchar("password", 50)
    val config = text("config")
    val status = enumerationByName("status", 32, TaskStatus::class).default(TaskStatus.WAITING)
    val exportType = enumerationByName("export_type", 10, ExportType::class).default(ExportType.PDF)
    val createTime = datetime("create_time").default(LocalDateTime.now())


    enum class TaskStatus {
        WAITING, PROCESSING, SUCCESS, FAILED
    }

    @Serializable
    enum class ExportType {
        PDF,IMG
    }
}

object Loggers : IntIdTable("logger") {
    val taskId = reference("task_id", Tasks)
    val level = enumerationByName("level", 32, LoggerLevel::class)
    val message = varchar("message", 200)
    val createTime = datetime("create_time").default(LocalDateTime.now())

    enum class LoggerLevel {
        INFO, WARN, ERROR
    }
}