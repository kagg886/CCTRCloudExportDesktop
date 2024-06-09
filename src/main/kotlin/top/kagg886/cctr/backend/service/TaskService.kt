package top.kagg886.cctr.backend.service

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.service.BaseService
import top.kagg886.cctr.backend.dao.Tasks

private val json = Json {
    allowStructuredMapKeys = true
    ignoreUnknownKeys = true
}

object TaskService : BaseService<Tasks, Task>(Tasks) {
    override fun toEntity(column: ResultRow): Task = Task(
        column[Tasks.id].value,
        column[Tasks.schoolId],
        column[Tasks.username],
        column[Tasks.password],
        column[Tasks.status],
        column[Tasks.exportType],
        column[Tasks.createTime],
        json.decodeFromString(column[Tasks.config]),
    )

    override fun Task.toDAO(state: UpdateBuilder<Int>) {
        state[Tasks.schoolId] = schoolId
        state[Tasks.username] = username
        state[Tasks.password] = password
        state[Tasks.status] = status
        state[Tasks.createTime] = createTime
        state[Tasks.config] = json.encodeToString(config)
        state[Tasks.exportType] = exportType
    }
}