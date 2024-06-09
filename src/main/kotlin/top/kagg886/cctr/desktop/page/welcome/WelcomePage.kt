package top.kagg886.cctr.desktop.page.welcome

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.viewmodel.viewModel
import top.kagg886.cctr.desktop.LocalNavigation
import top.kagg886.cctr.desktop.LocalNavigationShower
import top.kagg886.cctr.desktop.page.home.HOME_ROUTE
import top.kagg886.cctr.desktop.util.edge_config

@Composable
fun WelcomePage() {
    var show by LocalNavigationShower.current
    LaunchedEffect(Unit) {
        show = false
    }
    val edgeConfig by edge_config.watchAsState()

    val viewModel = viewModel(keys = listOf(edgeConfig.version)) {
        WelcomeViewModel().apply {
            dispatch(WelcomeViewModelAction.StartCheckingEdgeDriver(edgeConfig))
        }
    }
    val _state by viewModel.state.collectAsState()

    AnimatedContent(_state) { state->
        when (state) {
            WelcomeViewModelState.Default -> {}

            is WelcomeViewModelState.LoadingEdgeDriver -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val text by state.text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(text)
                    }
                }
            }

            is WelcomeViewModelState.LoadingFailure -> {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.weight(0.3f))
                    Column(modifier = Modifier.weight(1f).fillMaxWidth(0.8f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("请配置Microsoft Edge Driver", fontSize = 40.sp)
                        state.cause?.let {
                            Text(it, color = Color.Red)
                        }
                        var msg by remember {
                            mutableStateOf("")
                        }
                        OutlinedTextField(
                            value = msg,
                            onValueChange = { msg = it },
                            label = {
                                Text("请在这里输入Edge版本")
                            },
                            placeholder = {
                                Text("1.0.0")
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        ExtendedFloatingActionButton(
                            text = {
                                Text("OK!")
                            },
                            onClick = {
                                viewModel.dispatch(WelcomeViewModelAction.StartDownloadEdgeDriver(msg))
                            }
                        )
                        Spacer(modifier = Modifier.weight(0.1f))
                    }
                    Spacer(Modifier.weight(1f))
                }
            }

            WelcomeViewModelState.LoadingSuccess -> {
                val nav = LocalNavigation.current
                LaunchedEffect(Unit) {
                    nav.navigate(HOME_ROUTE)
                }
            }
        }

    }
}

const val WELCOME_ROUTE = "welcome"

fun RouteBuilder.configureWelcomePage() {
    scene(WELCOME_ROUTE) {
        WelcomePage()
    }
}