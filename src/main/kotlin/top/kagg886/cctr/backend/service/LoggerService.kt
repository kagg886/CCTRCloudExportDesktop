package top.kagg886.cctr.backend.service

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import top.kagg886.cctr.backend.dao.Logger
import top.kagg886.cctr.backend.dao.Loggers
import java.time.LocalDateTime

object LoggerService : BaseService<Loggers, Logger>(Loggers) {
    override fun toEntity(column: ResultRow): Logger {
        return Logger(
            column[Loggers.id].value,
            column[Loggers.taskId].value,
            column[Loggers.level],
            column[Loggers.message],
            column[Loggers.createTime]
        )
    }

    override fun Logger.toDAO(state: UpdateBuilder<Int>) {
        state[Loggers.taskId] = taskId
        state[Loggers.level] = level
        state[Loggers.message] = message
        state[Loggers.createTime] = createTime
    }

    suspend fun getLoggerByTaskId(taskId: Int, since: LocalDateTime? = null) = query {
        where {
            val con1 = Loggers.taskId eq taskId
            if (since != null) {
                return@where con1 and (Loggers.createTime greaterEq since)
            }
            con1
        }
    }

}

