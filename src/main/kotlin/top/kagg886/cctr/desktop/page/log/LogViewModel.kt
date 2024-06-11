package top.kagg886.cctr.desktop.page.log

import io.ktor.util.logging.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModelScope
import top.kagg886.cctr.backend.dao.Logger
import top.kagg886.cctr.backend.service.LoggerService
import top.kagg886.cctr.desktop.util.BaseAction
import top.kagg886.cctr.desktop.util.BaseState
import top.kagg886.cctr.desktop.util.BaseViewModel


private val log = KtorSimpleLogger("LogViewModel")

class LogViewModel : BaseViewModel<LogViewModelState, LogViewModelAction>() {
    override fun initState(): LogViewModelState = LogViewModelState.Loading

    override suspend fun onAction(state: LogViewModelState, action: LogViewModelAction) {
        when (action) {
            is LogViewModelAction.StartLoadingLog -> {
                viewModelScope.launch {
                    while (true) {
                        val log = LoggerService.getLoggerByTaskId(taskId = action.taskId)
                        setState(LogViewModelState.ShowLog(log))
                        delay(3000)
                    }
                }
            }
        }
    }
}

sealed interface LogViewModelState : BaseState {
    data object Loading : LogViewModelState
    data class ShowLog(val log: List<Logger>) : LogViewModelState
}

sealed interface LogViewModelAction : BaseAction {
    data class StartLoadingLog(val taskId: Int) : LogViewModelAction
}