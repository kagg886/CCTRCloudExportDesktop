package top.kagg886.cctr.desktop.page.welcome

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.kagg886.cctr.backend.dao.database
import top.kagg886.cctr.desktop.util.*
import top.kagg886.cctr.driver.WebDriverDispatcher
import top.kagg886.cctr.util.unzip
import top.kagg886.cctr.util.useTempFileSuspend
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

private val log = KtorSimpleLogger("WelcomeViewModel")
class WelcomeViewModel : BaseViewModel<WelcomeViewModelState, WelcomeViewModelAction>() {

    override fun initState(): WelcomeViewModelState = WelcomeViewModelState.Default

    override suspend fun onAction(state: WelcomeViewModelState, action: WelcomeViewModelAction) {
        when (action) {
            is WelcomeViewModelAction.StartCheckingEdgeDriver -> {
                log.info("start checking driver")
                val loading = WelcomeViewModelState.LoadingEdgeDriver()
                setState(loading)
                if (!checkEdgeDriverExists(action.config.version)) {
                    log.warn("edge driver: ${action.config.version} not exists")
                    setState(WelcomeViewModelState.LoadingFailure())
                    return
                }
                kotlin.runCatching {
                    loading.text.value = "初始化WebDriver..."
                    log.info("init edge driver...")
                    //Microsoft Edge WebDriver 125.0.2535.85 (xxx)
                    withContext(Dispatchers.IO) {
                        WebDriverDispatcher.init {
                            file = getEdgeDriverFile(action.config.version).resolve(msedgedriverName)
                            driverPoolSize = action.config.pool
                        }
                    }
                }.onFailure {
                    log.warn("init edge driver failed:",it)
                    setState(WelcomeViewModelState.LoadingFailure("驱动版本不兼容，请重新填写版本！"))
                }.onSuccess {
                    log.info("init edge driver success")
                    setState(WelcomeViewModelState.LoadingSuccess)
                }
            }

            is WelcomeViewModelAction.StartDownloadEdgeDriver -> {
                log.info("start checking url...")
                val loading = WelcomeViewModelState.LoadingEdgeDriver(mutableStateOf("检查url..."))
                setState(loading)

                val url = getEdgeDownloadURL(action.edgeVersion)
                HttpClient {
                    install(HttpTimeout) {
                        requestTimeoutMillis = 15.minutes.toLong(DurationUnit.MILLISECONDS)
                        connectTimeoutMillis = 15.minutes.toLong(DurationUnit.MILLISECONDS)
                        socketTimeoutMillis = 15.minutes.toLong(DurationUnit.MILLISECONDS)
                    }
                }.use {
                    it.prepareGet(url)
                        .execute {
                            val status = it.status
                            if (status.isSuccess().not()) {
                                log.warn("url $url not exists")
                                setState(
                                    WelcomeViewModelState.LoadingFailure("未找到Edge版本，请重试")
                                )
                                return@execute
                            }
                            val channel: ByteReadChannel = it.bodyAsChannel()
                            useTempFileSuspend { cancelFile ->
                                while (!channel.isClosedForRead) {
                                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                                    while (!packet.isEmpty) {
                                        val bytes = packet.readBytes()
                                        cancelFile.appendBytes(bytes)
                                        it.contentLength()?.let { length ->
                                            val buf = String.format(
                                                "%.2f",
                                                (cancelFile.length().toFloat() / length.toFloat()) * 100
                                            )
                                            loading.text.value = "Downloading... $buf%"
                                        }
                                    }
                                }
                                log.info("url $url download success, unzipping...")
                                loading.text.value = "Downloading... success!, unzipping..."
                                cancelFile.unzip(out = getEdgeDriverFile(action.edgeVersion))

                                setState(WelcomeViewModelState.LoadingSuccess)
                                edge_config.set {
                                    this.version = action.edgeVersion
                                }
                            }
                        }

                }
            }
        }
    }
}

sealed interface WelcomeViewModelState : BaseState {
    data object Default : WelcomeViewModelState

    data class LoadingEdgeDriver(
        var text: MutableState<String> = mutableStateOf("请稍后..."),
    ) : WelcomeViewModelState

    data object LoadingSuccess : WelcomeViewModelState
    data class LoadingFailure(
        val cause: String? = null,
    ) : WelcomeViewModelState
}

sealed interface WelcomeViewModelAction : BaseAction {
    data class StartCheckingEdgeDriver(
        val config: EdgeConfig
    ) : WelcomeViewModelAction

    data class StartDownloadEdgeDriver(val edgeVersion: String) : WelcomeViewModelAction
}