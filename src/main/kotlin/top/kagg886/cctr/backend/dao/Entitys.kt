package top.kagg886.cctr.backend.dao

import kotlinx.serialization.Serializable
import top.kagg886.cctr.api.modules.ChapterType
import top.kagg886.cctr.api.modules.Practice
import top.kagg886.cctr.api.modules.QuestionType
import top.kagg886.cctr.backend.dao.Loggers
import top.kagg886.cctr.backend.dao.Tasks
import top.kagg886.cctr.backend.util.LocalDateTimeSerializer
import java.time.LocalDateTime

interface BaseEntity {
    val id: Int
}


@Serializable
data class Task(
    override val id: Int,
    val schoolId: String,
    val username: String,
    val password: String,
    val status: Tasks.TaskStatus,
    val exportType: Tasks.ExportType,
    @Serializable(with = LocalDateTimeSerializer::class) val createTime: LocalDateTime,
    //课程名称 课程配置
    val config: Map<Practice, Map<ChapterType, List<QuestionType>>>
) : BaseEntity {

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }
        if (other as? Task == null) {
            return false
        }
        return this === other || id == other.id
    }

    // 注意：你也需要提供一个对应的hashCode()方法以保持equals()和hashCode()的一致性
    override fun hashCode(): Int {
        return id.hashCode()
    }
}

@Serializable
data class Logger(
    override val id: Int,
    val taskId: Int,
    val level: Loggers.LoggerLevel,
    val message: String,
    @Serializable(with = LocalDateTimeSerializer::class) val createTime: LocalDateTime,
) : BaseEntity