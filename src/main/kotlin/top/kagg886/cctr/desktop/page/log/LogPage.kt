package top.kagg886.cctr.desktop.page.log

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Notification
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.viewmodel.viewModel
import top.kagg886.cctr.backend.dao.Loggers
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.LocalNavigationShower

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LogPage(taskId: String, goBack: () -> Unit = {}) {
    val model = viewModel(keys = listOf(taskId)) {
        LogViewModel().apply {
            dispatch(LogViewModelAction.StartLoadingLog(taskId.toInt()))
        }
    }
    val state by model.state.collectAsState()

    when (state) {
        LogViewModelState.Loading -> {

        }

        is LogViewModelState.ShowLog -> {
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = { goBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "go_back")
                }
                LazyColumn {
                    items((state as LogViewModelState.ShowLog).log) {
                        var showMultiLine by remember {
                            mutableStateOf(false)
                        }
                        ListItem(
                            icon = {
                                Icon(
                                    when (it.level) {
                                        Loggers.LoggerLevel.INFO -> {
                                            Icons.Default.Info
                                        }

                                        Loggers.LoggerLevel.WARN -> {
                                            Icons.Default.Warning
                                        }

                                        Loggers.LoggerLevel.ERROR -> {
                                            Icons.Default.Close
                                        }
                                    }, ""
                                )
                            },
                            trailing = {
                                Text("${it.createTime}")
                            },
                            modifier = Modifier.clickable {
                                showMultiLine = !showMultiLine
                            }
                        ) {
                            Text(
                                it.message,
                                overflow = if (showMultiLine) TextOverflow.Visible else TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

const val LOG_ROUTE = "log/{id}"

fun RouteBuilder.configureLogPage() {
    scene(
        route = LOG_ROUTE,
        navTransition = NavTransition(
            createTransition = slideInHorizontally { it } + fadeIn(),
            destroyTransition = slideOutVertically { it } + fadeOut()
        )
    ) {
        var show by LocalNavigationShower.current
        LaunchedEffect(Unit) {
            show = false
        }
        val nav = LocalNavigation.current
        LogPage(it.path<String>("id")!!) {
            show = true
            nav.goBack()
        }
    }
}