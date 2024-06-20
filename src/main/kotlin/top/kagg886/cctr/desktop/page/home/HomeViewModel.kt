package top.kagg886.cctr.desktop.page.home

import io.ktor.util.logging.*
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.service.TaskService
import top.kagg886.cctr.backend.util.paged
import top.kagg886.cctr.desktop.util.BaseAction
import top.kagg886.cctr.desktop.util.BaseState
import top.kagg886.cctr.desktop.util.BaseViewModel
import java.util.Random

private val log = KtorSimpleLogger("HomeViewModel")

class HomeViewModel : BaseViewModel<HomeViewModelState, HomeViewModelAction>() {
    override fun initState(): HomeViewModelState = HomeViewModelState.Default

    override suspend fun onAction(state: HomeViewModelState, action: HomeViewModelAction) {
        when (action) {
            is HomeViewModelAction.LoadingData -> {
                log.info("start sql query: ${action.pageIndex}")
                val result = TaskService.paged(page = action.pageIndex.toLong(), size = 15, reversed = true)
                log.info("sql query success: $result")
                setState(
                    HomeViewModelState.LoadingSuccess(
                        list = result.data,
                        pageIndex = action.pageIndex,
                        total = result.count.toInt()
                    )
                )
            }
        }
    }

}

sealed interface HomeViewModelState : BaseState {
    data object Default : HomeViewModelState
    data object Loading : HomeViewModelState
    data class LoadingSuccess(val list: List<Task>, val pageIndex: Int, val total: Int,val dirty:Int = Random().nextInt()) : HomeViewModelState
}

sealed interface HomeViewModelAction : BaseAction {
    data class LoadingData(val pageIndex: Int) : HomeViewModelAction
}