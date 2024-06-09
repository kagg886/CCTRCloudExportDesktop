package top.kagg886.cctr.backend.util

import io.ktor.util.logging.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.service.LoggerService
import top.kagg886.cctr.backend.dao.Loggers
import top.kagg886.cctr.backend.dao.Logger
import java.time.LocalDateTime

private val scope = CoroutineScope(Dispatchers.IO)
private val logger = KtorSimpleLogger("TaskLogger")

fun Task.log(level: Loggers.LoggerLevel, msg: String) {
    when(level) {
        Loggers.LoggerLevel.INFO -> logger.info("[Task:${id}]: $msg")
        Loggers.LoggerLevel.WARN -> logger.warn("[Task:${id}]: $msg")
        Loggers.LoggerLevel.ERROR -> logger.error("[Task:${id}]: $msg")
    }
    scope.launch {
        LoggerService.insert(
            Logger(
                id = -1,
                taskId = id,
                level = level,
                message = msg,
                createTime = LocalDateTime.now()
            )
        )
    }
}
fun Task.info(msg:String) = log(Loggers.LoggerLevel.INFO,msg)
fun Task.warn(msg:String) = log(Loggers.LoggerLevel.WARN,msg)
fun Task.error(msg:String) = log(Loggers.LoggerLevel.ERROR,msg)