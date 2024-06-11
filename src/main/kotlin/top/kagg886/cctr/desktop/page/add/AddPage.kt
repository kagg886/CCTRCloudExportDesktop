package top.kagg886.cctr.desktop.page.add

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.viewmodel.viewModel
import top.kagg886.cctr.api.modules.ChapterType
import top.kagg886.cctr.api.modules.Practice
import top.kagg886.cctr.api.modules.QuestionType
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.service.TaskService
import top.kagg886.cctr.backend.dao.Tasks
import top.kagg886.cctr.backend.dao.Tasks.exportType
import top.kagg886.cctr.backend.dao.Tasks.schoolId
import top.kagg886.cctr.backend.task.TaskManager
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.LocalNavigationShower
import top.kagg886.cctr.desktop.LocalSnackBar
import top.kagg886.cctr.desktop.page.home.HOME_ROUTE
import java.time.LocalDateTime

@Composable
fun AddPage(goBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            IconButton(onClick = {goBack()}) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "go_back")
            }

            val model = viewModel {
                AddViewModel()
            }

            val _state by model.state.collectAsState()
            AnimatedContent(_state, modifier = Modifier.fillMaxSize()) {state->
                when(state) {
                    is AddViewModelState.LoginToCCTR -> {
                        val scope = rememberCoroutineScope()
                        val snack = LocalSnackBar.current
                        LaunchedEffect(Unit) {
                            if (state.msg.isNotBlank()) {
                                scope.launch {
                                    snack.showSnackbar(state.msg)
                                }
                            }
                        }
                        Column(modifier = Modifier.fillMaxWidth(0.8f),horizontalAlignment = Alignment.CenterHorizontally) {
                            var schoolId by remember { mutableStateOf("") }
                            var userName by remember { mutableStateOf("") }
                            var password by remember { mutableStateOf("") }
                            Spacer(modifier = Modifier.weight(1f))
                            OutlinedTextField(
                                value = schoolId,
                                onValueChange = { schoolId = it },
                                label = { Text("学校代号") },
                            )
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("用户名") }
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("密码") }
                            )
                            Spacer(Modifier.weight(1f))
                            FloatingActionButton(onClick = {
                                model.dispatch(AddViewModelAction.LoginToCCTR(schoolId,userName, password))
                            }){
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                    AddViewModelState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Text("Loading...")
                            }
                        }
                    }
                    is AddViewModelState.WaitChoose -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            val conf = remember {
                                mutableStateOf<Map<Practice,Map<ChapterType,List<QuestionType>>>>(mapOf())
                            }
                            ChooseConfig(user = state.client, config = conf)
                            FloatingActionButton(
                                onClick = {
                                   model.dispatch(AddViewModelAction.SendConfig(user = state.client,config = conf.value))
                                },
                                modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "next")
                            }
                        }
                    }

                    is AddViewModelState.SelectExportType -> {
                        //TODO 导出方式
                        Column(modifier = Modifier.fillMaxWidth(0.8f),horizontalAlignment = Alignment.CenterHorizontally) {

                            Spacer(modifier = Modifier.weight(1f))
                            val radioOptions = Tasks.ExportType.entries.toList()
                            val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
                            Text("选择你的导出方式")
                            Column(Modifier.selectableGroup()) {
                                radioOptions.forEach { text ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(56.dp)
                                            .selectable(
                                                selected = (text == selectedOption),
                                                onClick = { onOptionSelected(text) },
                                                role = Role.RadioButton
                                            )
                                            .padding(horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = (text == selectedOption),
                                            onClick = null
                                        )
                                        Text(
                                            text = text.name,
                                            style = MaterialTheme.typography.body1.merge(),
                                            modifier = Modifier.padding(start = 16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            val navigator = LocalNavigation.current
                            val scope = rememberCoroutineScope()
                            val snack = LocalSnackBar.current
                            FloatingActionButton(onClick = {
                                scope.launch {
                                    val task = TaskManager.commitTask(Task(
                                        id = -1,
                                        schoolId = state.client.schoolId,
                                        username = state.client.userName,
                                        password = state.client.passWord,
                                        status = Tasks.TaskStatus.WAITING,
                                        exportType = selectedOption,
                                        createTime = LocalDateTime.now(),
                                        config = state.config
                                    ))
                                    snack.showSnackbar("提交成功!，任务id为：${task.id}")
                                    navigator.navigate(HOME_ROUTE)
                                }
//                                model.dispatch(AddViewModelAction.StartToExport(state.client,state.config,selectedOption))
                            }){
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "")
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

}

const val ADD_ROUTE = "ADD_ROUTE"

fun RouteBuilder.configureAddPage() {
    scene(ADD_ROUTE, navTransition = NavTransition(
        createTransition = slideInHorizontally { it } + fadeIn(),
        destroyTransition = slideOutHorizontally { it }  + fadeOut()
    )) {
        val nav = LocalNavigation.current
        var show by LocalNavigationShower.current
        DisposableEffect(Unit) {
            show = false
            onDispose {
                show = true
            }
        }
        AddPage {
            nav.goBack()
        }
    }
}