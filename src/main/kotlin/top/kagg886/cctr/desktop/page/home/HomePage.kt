package top.kagg886.cctr.desktop.page.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.viewmodel.viewModel
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.dao.Tasks
import top.kagg886.cctr.backend.task.TaskManager
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.LocalNavigationShower
import top.kagg886.cctr.desktop.LocalSnackBar
import top.kagg886.cctr.desktop.page.add.ADD_ROUTE
import top.kagg886.cctr.desktop.page.log.LOG_ROUTE
import top.kagg886.cctr.desktop.util.CCTRConfig
import top.kagg886.cctr.desktop.util.cctr_last_modifier
import top.kagg886.cctr.desktop.util.root_file
import java.awt.Desktop
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

@Composable
fun HomePage() {
    var show by LocalNavigationShower.current
    LaunchedEffect(Unit) {
        show = true
    }
    val model = viewModel {
        HomeViewModel().apply {
            dispatch(HomeViewModelAction.LoadingData(1))
        }
    }
    val state by model.state.collectAsState()

    when (state) {
        HomeViewModelState.Default -> {}

        HomeViewModelState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Text("加载中...")
                }
            }
        }

        is HomeViewModelState.LoadingSuccess -> {
            val snack = LocalSnackBar.current
            val scope = rememberCoroutineScope()
            LaunchedEffect(state) {
                scope.launch {
                    snack.showSnackbar("加载成功!")
                }
            }
            val state = state as HomeViewModelState.LoadingSuccess

            Box(Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(listOf("任务id", "学校代号", "用户名", "密码", "任务状态", "导出模式", "创建时间", "操作")) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(it, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Divider()
                        }
                    }
                    for (i in state.list) {
                        item {
                            Text(i.id.toString(), textAlign = TextAlign.Center)
                        }
                        item {
                            Text(i.schoolId, textAlign = TextAlign.Center)
                        }
                        item {
                            Text(i.username, textAlign = TextAlign.Center)
                        }
                        item {
                            Text(i.password, textAlign = TextAlign.Center)
                        }
                        item {
                            Text(i.status.toString(), textAlign = TextAlign.Center)
                        }
                        item {
                            Text(i.exportType.toString(), textAlign = TextAlign.Center)
                        }
                        item {
                            Text(i.createTime.toString(), textAlign = TextAlign.Center)
                        }
                        item {
                            val drop = remember {
                                DropdownMenuState()
                            }
                            DropdownMenu(drop) {
                                val nav = LocalNavigation.current
                                DropdownMenuItem(onClick = {
                                    nav.navigate("log/${i.id}")
                                }) {
                                    Text("查看日志")
                                }
                                var dialog by remember {
                                    mutableStateOf(false)
                                }
                                if (dialog) {
                                    Dialog(onDismissRequest = {
                                        dialog = false
                                    }) {
                                        Surface(
                                            modifier = Modifier.fillMaxSize(0.7f)
                                        ) {
                                            LazyColumn {
                                                item {
                                                    Text(i.config.toString(), textAlign = TextAlign.Center)
                                                }
                                            }
                                        }
                                    }
                                }

                                DropdownMenuItem(onClick = {
                                    dialog = true
                                }) {
                                    Text("查看配置")
                                }
                                DropdownMenuItem(onClick = {
                                    val f = root_file.resolve("out").resolve("task_" + i.id.toString() + ".zip")
                                    if (f.exists().not()) {
                                        scope.launch {
                                            snack.showSnackbar("文件不存在。可能是文件正在导出，或文件已删除")
                                        }
                                        return@DropdownMenuItem
                                    }
                                    Desktop.getDesktop().open(f);
                                }) {
                                    Text("打开文件")
                                }

                                DropdownMenuItem(onClick = {
                                    scope.launch {
                                        val config = CCTRConfig(i.schoolId, i.username, i.password)
                                        val task = TaskManager.commitTask(
                                            Task(
                                                id = -1,
                                                schoolId = config.schoolId,
                                                username = config.userName,
                                                password = config.password,
                                                status = Tasks.TaskStatus.WAITING,
                                                exportType = i.exportType,
                                                createTime = LocalDateTime.now(),
                                                config = i.config
                                            )
                                        )
                                        launch {
                                            snack.showSnackbar("提交成功!，任务id为：${task.id}")
                                        }
                                        nav.navigate(HOME_ROUTE)
                                    }
                                }) {
                                    Text("重试")
                                }
                            }
                            var offset by remember {
                                mutableStateOf<Offset?>(null)
                            }
                            TextButton(onClick = {
                                drop.status = DropdownMenuState.Status.Open(offset!!)
                            }, modifier = Modifier.onGloballyPositioned {
                                offset = it.localToWindow(Offset.Zero)
                            }) {
                                Text("打开菜单")
                            }
                        }
                    }
                }
                var expand by remember { mutableStateOf(false) }
                val pageCount = state.total / 15 + 1
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
                    visible = expand,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    Surface(
                        shape = CircleShape.copy(all = CornerSize(size = 10.dp)),
                        color = Color.Gray.copy(alpha = 0.2f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    model.dispatch(HomeViewModelAction.LoadingData(max(1, state.pageIndex - 1)))
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "left")
                            }
                            Text(
                                "第${state.pageIndex}页，共${pageCount}页",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                            IconButton(
                                onClick = {
                                    model.dispatch(HomeViewModelAction.LoadingData(min(pageCount, state.pageIndex + 1)))
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "right")
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = expand,
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut(),
                    ) {
                        Column {
                            val nav = LocalNavigation.current
                            FloatingActionButton(
                                onClick = {
                                    nav.navigate(route = ADD_ROUTE)
                                },
                            ) {
                                Icon(Icons.Default.Add, "add")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            FloatingActionButton(
                                onClick = {
                                    model.dispatch(HomeViewModelAction.LoadingData(state.pageIndex))
                                },
                            ) {
                                Icon(Icons.Default.Refresh, "refersh")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            expand = !expand
                        },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Icon(Icons.Default.Menu, "menu")
                    }
                }
            }
        }
    }
}

const val HOME_ROUTE = "homepage"

fun RouteBuilder.configureHomePage() {
    scene(HOME_ROUTE) {
        HomePage()
    }
}