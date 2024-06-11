package top.kagg886.cctr.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import io.ktor.util.logging.*
import kotlinx.coroutines.delay
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator
import top.kagg886.cctr.backend.task.TaskManager
import top.kagg886.cctr.desktop.component.Navigation
import top.kagg886.cctr.desktop.page.add.configureAddPage
import top.kagg886.cctr.desktop.page.home.configureHomePage
import top.kagg886.cctr.desktop.page.welcome.WELCOME_ROUTE
import top.kagg886.cctr.desktop.page.welcome.configureWelcomePage

val LocalNavigationShower = compositionLocalOf<MutableState<Boolean>> { error("LocalNavigationShower not provided") }
val LocalNavigation = compositionLocalOf<Navigator> { error("LocalNavigation not provided") }
val LocalSnackBar = compositionLocalOf<SnackbarHostState> { error("LocalSnackBar not provided") }
val LocalTray = compositionLocalOf<TrayState> { error("LocalTray not provided") }
private var log = KtorSimpleLogger("log")
fun main() {
    log.info("Application started")

    TaskManager.start()
    application {
        Window(onCloseRequest = ::exitApplication) {
            PreComposeApp {
                CompositionLocalProvider(
                    LocalNavigationShower provides remember {
                        mutableStateOf(false)
                    },
                    LocalNavigation provides rememberNavigator(),
                    LocalSnackBar provides remember {
                        SnackbarHostState()
                    },
                    LocalTray provides rememberTrayState()
                ) {
                    MaterialTheme {
                        Tray(
                            icon = painterResource("a.png"),
                            state = LocalTray.current
                        ) {
                            Item(
                                "退出程序",
                                onClick = {
                                    exitApplication()
                                }
                            )
                        }
                        App()
                    }
                }
            }
        }
    }
    log.info("Application finished")
}

@Composable
fun App() {
    val isShowNavigation by LocalNavigationShower.current

    Row {
        AnimatedContent(targetState = isShowNavigation) {
            if (it) {
                Navigation()
            }
        }
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = LocalSnackBar.current
                )
            }
        ) {
            NavHost(
                modifier = Modifier.fillMaxWidth().padding(it),
                navigator = LocalNavigation.current,
                initialRoute = WELCOME_ROUTE,
            ) {
                configureWelcomePage()
                configureHomePage()
                configureAddPage()
            }
        }
    }
}