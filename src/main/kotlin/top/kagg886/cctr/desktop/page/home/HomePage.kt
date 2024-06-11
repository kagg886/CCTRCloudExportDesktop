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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.viewmodel.viewModel
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.LocalNavigationShower
import top.kagg886.cctr.desktop.LocalSnackBar
import top.kagg886.cctr.desktop.page.add.ADD_ROUTE
import top.kagg886.cctr.desktop.page.log.LOG_ROUTE
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
                    GridCells.Fixed(9),
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(listOf("任务id","学校代号","用户名","密码","配置文件","任务状态","导出模式","创建时间","操作")) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(it,fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
                            TextButton(onClick = {
                                dialog = true
                            }) {
                                Text("查看配置")
                            }
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
                            Row {
                                val nav = LocalNavigation.current
                                TextButton(onClick = {
                                    nav.navigate("log/${i.id}")
                                }) {
                                    Text("查看日志")
                                }
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
                                    model.dispatch(HomeViewModelAction.LoadingData(max(1,state.pageIndex-1)))
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft,"left")
                            }
                            Text("第${state.pageIndex}页，共${pageCount}页", fontSize = 20.sp, modifier = Modifier.padding(8.dp))
                            IconButton(
                                onClick = {
                                    model.dispatch(HomeViewModelAction.LoadingData(min(pageCount,state.pageIndex+1)))
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight,"right")
                            }
                        }
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomEnd).padding(32.dp),horizontalAlignment = Alignment.CenterHorizontally) {
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