package top.kagg886.cctr.desktop.page.add

import io.ktor.util.logging.*
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.*
import top.kagg886.cctr.desktop.util.BaseAction
import top.kagg886.cctr.desktop.util.BaseState
import top.kagg886.cctr.desktop.util.BaseViewModel


private val log = KtorSimpleLogger("ChooseConfigViewModel")

class ChooseConfigViewModel : BaseViewModel<ChooseConfigViewModelState, ChooseConfigViewModelAction>() {

    override fun initState(): ChooseConfigViewModelState = ChooseConfigViewModelState.Default

    override suspend fun onAction(state: ChooseConfigViewModelState, action: ChooseConfigViewModelAction) {
        when (action) {
            is ChooseConfigViewModelAction.LoadALLPracticeForString -> {
                val practiceList = action.user.getUserPracticeList()
                setState(ChooseConfigViewModelState.LoadSuccess(
                    config = buildMap {
                        practiceList.forEach {
                            this[it] = mutableMapOf()
                        }
                    },
                    currentPractice = null,
                    currentChapterList = null,
                    currentChapterType = null,
                    questionList = null,
                    user = action.user
                ))
            }

            is ChooseConfigViewModelAction.SetAllByPractice -> {
                val s = (state as ChooseConfigViewModelState.LoadSuccess)
                val newConfig = s.config.toMutableMap()

                with(action.target) {
                    val (cType,qType) = state.user.getQuestionType(this)
                    newConfig[this] = buildMap {
                        for (c in cType) {
                            this[c] = qType.toList()
                        }
                    }
                }


                val newVal = s.copy(config = newConfig)
                setState(
                    newVal
                )
            }

            is ChooseConfigViewModelAction.DeSetAllByPractice -> {
                val s = (state as ChooseConfigViewModelState.LoadSuccess)
                val newConfig = s.config.toMutableMap()
                with(action.target) {
                    newConfig[this] = mapOf()
                }
                val newVal = s.copy(config = newConfig)
                setState(
                    newVal
                )
            }

            is ChooseConfigViewModelAction.SelectPractice -> {
                (state as ChooseConfigViewModelState.LoadSuccess)
                val (cType,_) = state.user.getQuestionType(action.practice)
                setState(
                    state.copy(
                        currentPractice = action.practice,
                        currentChapterList = buildMap { 
                            for (i in cType) {
                                this[i] = listOf()
                            }
                        }
                    )
                )
            }
            is ChooseConfigViewModelAction.SetAllByChapterType -> {
                (state as ChooseConfigViewModelState.LoadSuccess)
                val (_,qType) = state.user.getQuestionType(state.currentPractice!!)

                val newVal = state.config.toMutableMap().apply {
                    this[state.currentPractice] = (this[state.currentPractice]?.toMutableMap()?: mutableMapOf()).apply {
                        this[action.target] = qType.toList()
                    }
                }
                setState(
                    state.copy(
                        config = newVal
                    )
                )
            }
            is ChooseConfigViewModelAction.DeSetAllByChapterType -> {
                (state as ChooseConfigViewModelState.LoadSuccess)

                val newConfig = state.config.toMutableMap()

                val map = newConfig[state.currentPractice!!]!!.toMutableMap()
                map.remove(action.target)


                if (map.isNotEmpty()) {
                    newConfig[state.currentPractice] = map
                } else {
                    newConfig[state.currentPractice] = mapOf()
                }

                setState(
                    state.copy(
                        config = newConfig
                    )
                )
            }
            is ChooseConfigViewModelAction.SelectChapterType -> {
                (state as ChooseConfigViewModelState.LoadSuccess)
                val (_,qType) = state.user.getQuestionType(state.currentPractice!!)
                setState(
                    state.copy(
                        currentChapterType = action.target,
                        questionList = qType.toList()
                    )
                )
            }

            is ChooseConfigViewModelAction.DeSelectQuestionType -> {
                (state as ChooseConfigViewModelState.LoadSuccess)

                val newConfig = state.config.toMutableMap()
                val pMap = newConfig[state.currentPractice!!]!!.toMutableMap()
                val qList = pMap[state.currentChapterType]!!.toMutableList()

                qList.remove(action.qType)

                if (qList.isNotEmpty()) {
                    pMap[state.currentChapterType!!] = qList
                } else {
                    pMap.remove(state.currentChapterType)
                }

                if (pMap.isNotEmpty()) {
                    newConfig[state.currentPractice] = pMap
                } else {
                    newConfig[state.currentPractice] = mapOf()
                }
                setState(
                    state.copy(
                        config = newConfig
                    )
                )
            }
            is ChooseConfigViewModelAction.SelectQuestionType -> {
                (state as ChooseConfigViewModelState.LoadSuccess)
                setState(state.copy(
                    config = state.config.toMutableMap().apply {
                        this[state.currentPractice!!] = this[state.currentPractice]!!.toMutableMap().apply {
                            this[state.currentChapterType!!] = (this[state.currentChapterType]?.toMutableList() ?: mutableListOf()).apply {
                                this.add(action.qType)
                            }
                        }
                    }
                ))
            }
        }
    }
}

sealed interface ChooseConfigViewModelState : BaseState {
    data object Default: ChooseConfigViewModelState
    data class LoadSuccess(
        val user: CCTRUser,
        val config:Map<Practice,Map<ChapterType,List<QuestionType>>>,
        val currentPractice: Practice?,
        val currentChapterList:Map<ChapterType,List<QuestionType>>?,
        val currentChapterType: ChapterType?,

        val questionList: List<QuestionType>?
    ): ChooseConfigViewModelState
}

sealed interface ChooseConfigViewModelAction : BaseAction {
    data class LoadALLPracticeForString(val user:CCTRUser):ChooseConfigViewModelAction
    
    data class SetAllByPractice(val target:Practice): ChooseConfigViewModelAction
    data class DeSetAllByPractice(val target:Practice): ChooseConfigViewModelAction
    data class SelectPractice(val practice: Practice):ChooseConfigViewModelAction


    data class SetAllByChapterType(val target:ChapterType): ChooseConfigViewModelAction
    data class DeSetAllByChapterType(val target:ChapterType): ChooseConfigViewModelAction
    data class SelectChapterType(val target: ChapterType):ChooseConfigViewModelAction

    data class SelectQuestionType(val qType:QuestionType):ChooseConfigViewModelAction
    data class DeSelectQuestionType(val qType:QuestionType):ChooseConfigViewModelAction
}