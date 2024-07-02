package top.kagg886.cctr.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.*
import io.ktor.util.logging.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator
import top.kagg886.cctr.backend.task.TaskManager
import top.kagg886.cctr.desktop.component.Navigation
import top.kagg886.cctr.desktop.page.add.configureAddPage
import top.kagg886.cctr.desktop.page.home.configureHomePage
import top.kagg886.cctr.desktop.page.log.configureLogPage
import top.kagg886.cctr.desktop.page.welcome.WELCOME_ROUTE
import top.kagg886.cctr.desktop.page.welcome.configureWelcomePage
import top.kagg886.cctr.desktop.util.root_file

val LocalNavigationShower = compositionLocalOf<MutableState<Boolean>> { error("LocalNavigationShower not provided") }
val LocalNavigation = compositionLocalOf<Navigator> { error("LocalNavigation not provided") }
val LocalSnackBar = compositionLocalOf<SnackbarHostState> { error("LocalSnackBar not provided") }
private val LocalTray = compositionLocalOf<TrayState> { error("LocalTray not provided") }
val trayChannel = Channel<String>()

private var log = KtorSimpleLogger("log")
@OptIn(DelicateCoroutinesApi::class)
fun main() {
    System.setProperty("log.path", "$root_file/log")
    log.info("""
        Application prepare start
        
        java-info:
            version: ${System.getProperty("java.version")}
            vendor: ${System.getProperty("java.vendor")}(${System.getProperty("java.vendor.url")})
            home: ${System.getProperty("java.home")}
        user-info:
            root: ${System.getProperty("user.home")}
    """.trimIndent())

    TaskManager.start()
    if ((root_file.canRead() && root_file.canWrite()).not()) {
        application {
            Window(onCloseRequest = ::exitApplication) {
                Text("请授予程序在${root_file.absolutePath}的读写权限！")
            }
        }
        return
    }
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
                    val tray = LocalTray.current
                    val scope = rememberCoroutineScope()
                    scope.launch {
                        while (!trayChannel.isClosedForReceive) {
                            val msg = trayChannel.receive()
                            tray.sendNotification(Notification("通知",msg))
                        }
                    }
                    MaterialTheme {
                        Tray(
                            icon = painterResource("a.png"),
                            state = tray
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
                configureLogPage()
            }
        }
    }
}