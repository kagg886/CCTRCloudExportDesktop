package top.kagg886.cctr.desktop.page.add

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import moe.tlaster.precompose.viewmodel.viewModel
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.ChapterType
import top.kagg886.cctr.api.modules.Practice
import top.kagg886.cctr.api.modules.QuestionType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChooseConfig(user:CCTRUser,config:MutableState<Map<Practice,Map<ChapterType,List<QuestionType>>>>) {
    val model = viewModel(keys = listOf(user)) {
        ChooseConfigViewModel().apply {
            dispatch(ChooseConfigViewModelAction.LoadALLPracticeForString(user))
        }
    }
    val _state by model.state.collectAsState()

    LaunchedEffect(_state) {
        if (_state is ChooseConfigViewModelState.LoadSuccess) {
            config.value = (_state as ChooseConfigViewModelState.LoadSuccess).config
        }
    }

    AnimatedContent(_state) { state->
        when(state) {
            ChooseConfigViewModelState.Default -> {}
            is ChooseConfigViewModelState.LoadSuccess -> {
                if (state.loading) {
                    Dialog(onDismissRequest = {}) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                val config = state.config
                Row(Modifier.fillMaxSize().padding(top = 50.dp)) {
                    Column(Modifier.weight(1f)) {
                        LazyColumn {
                            item {
                                Row {
                                    TextButton(onClick = {
                                        model.dispatch(ChooseConfigViewModelAction.SetAllPractice)
                                    }){
                                        Text("全选")
                                    }

                                    TextButton(onClick = {
                                        config.keys.forEach {
                                            model.dispatch(ChooseConfigViewModelAction.DeSetAllByPractice(it))
                                        }
                                    }) {
                                        Text("反选")
                                    }
                                }
                            }
                            items(config.keys.toList()) { practice->
                                ListItem(
                                    icon = {
                                        val checked = state.config[practice]!!.isNotEmpty()
                                        Checkbox(
                                            checked = checked,
                                            onCheckedChange = {
                                                model.dispatch(
                                                    if (it) ChooseConfigViewModelAction.SetAllByPractice(practice) else ChooseConfigViewModelAction.DeSetAllByPractice(practice)
                                                )
                                            },
                                        )
                                    },
                                    text = {
                                        Text(buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontSize = 15.sp)) {
                                                append(practice.practiceName)
                                            }
                                            appendLine()
                                            withStyle(style = SpanStyle(fontSize = 13.sp)) {
                                                append(practice.lessonName)
                                            }
                                        })
                                    },
                                    modifier = Modifier.clickable {
                                         model.dispatch(ChooseConfigViewModelAction.SelectPractice(practice))
                                    }.background(if (state.currentPractice == practice) Color.Unspecified.copy(alpha = 0.1f) else Color.Unspecified)
                                )
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        state.currentChapterList?.let {
                            LazyColumn {
                                items(it.keys.toList()) { key->
                                    ListItem(
                                        icon = {
                                            val checked = state.config[state.currentPractice]?.contains(key) ?: false
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = {
                                                    model.dispatch(
                                                        if (it) ChooseConfigViewModelAction.SetAllByChapterType(key) else ChooseConfigViewModelAction.DeSetAllByChapterType(key)
                                                    )
                                                },
                                            )
                                        },
                                        text = {
                                            Text(key.name)
                                        },
                                        modifier = Modifier.clickable {
                                            model.dispatch(ChooseConfigViewModelAction.SelectChapterType(key))
                                        }.background(if (state.currentChapterType == key) Color.Unspecified.copy(alpha = 0.2f) else Color.Unspecified.copy(alpha = 0.1f))
                                    )
                                }
                            }
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        state.questionList?.let {
                            LazyColumn {
                                items(it) { qType->
                                    ListItem(
                                        icon = {
                                            val checked = config[state.currentPractice!!]?.get(state.currentChapterType!!)?.contains(qType)?: false
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = {
                                                    model.dispatch(
                                                        if (it) ChooseConfigViewModelAction.SelectQuestionType(qType) else ChooseConfigViewModelAction.DeSelectQuestionType(qType)
                                                    )
                                                },
                                            )
                                        },
                                        text = {
                                            Text(qType.name)
                                        },
                                        modifier = Modifier.background(Color.Unspecified.copy(alpha = 0.2f))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}