package top.kagg886.cctr.desktop.page.add

import io.ktor.util.logging.*
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.ChapterType
import top.kagg886.cctr.api.modules.Practice
import top.kagg886.cctr.api.modules.QuestionType
import top.kagg886.cctr.backend.dao.Tasks
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.util.BaseAction
import top.kagg886.cctr.desktop.util.BaseState
import top.kagg886.cctr.desktop.util.BaseViewModel
import top.kagg886.cctr.desktop.util.cctr_last_modifier


private val log = KtorSimpleLogger("AddViewModel")

class AddViewModel : BaseViewModel<AddViewModelState, AddViewModelAction>() {
    override fun initState(): AddViewModelState = AddViewModelState.LoginToCCTR()

    override suspend fun onAction(state: AddViewModelState, action: AddViewModelAction) {
        when (action) {
            AddViewModelAction.LoginToCCTR -> {
                setState(AddViewModelState.Loading)
                val login = cctr_last_modifier.value()
                log.info("start to login CCTR: $login")
                kotlin.runCatching {
                    CCTRUser.newCCTRUser {
                        schoolId = login.schoolId
                        username = login.userName
                        password = login.password
                    }
                }.onFailure {
                    setState(AddViewModelState.LoginToCCTR(it.message!!))
                }.onSuccess {
                    log.info("login to CCTR successful")
                    setState(AddViewModelState.WaitChoose(it))
                }
            }

            is AddViewModelAction.SendConfig -> {
                setState(AddViewModelState.SelectExportType(action.config))
            }
        }
    }

}

sealed interface AddViewModelState : BaseState {
    data object Loading : AddViewModelState
    data class LoginToCCTR(val msg: String = "") : AddViewModelState
    data class WaitChoose(val client: CCTRUser) : AddViewModelState
    data class SelectExportType(val config: Map<Practice, Map<ChapterType, List<QuestionType>>>) : AddViewModelState
}

sealed interface AddViewModelAction : BaseAction {
    data object LoginToCCTR : AddViewModelAction
    data class SendConfig(val user: CCTRUser,val config: Map<Practice, Map<ChapterType, List<QuestionType>>>) : AddViewModelAction
}