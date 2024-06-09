package top.kagg886.cctr.driver

import io.ktor.util.logging.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.openqa.selenium.edge.EdgeDriver
import java.io.File

class DispatcherConfig {
    var driverPoolSize = 10
    var file: File? = null
}

private val log = KtorSimpleLogger("WebDriverDispatcher")

object WebDriverDispatcher {
    private lateinit var queue:Channel<EdgeDriver>
    private val list = mutableListOf<EdgeDriver>()

    suspend fun init(conf: DispatcherConfig.() -> Unit) {
        val config = DispatcherConfig().apply(conf)
        WebDriverProducer.init(config.file!!)
        queue = Channel(capacity = config.driverPoolSize);
        for (i in 1..config.driverPoolSize) {
            WebDriverProducer.newHeadlessDriver().apply {
                list.add(this)
                queue.send(this)
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread {
            runBlocking(Dispatchers.IO) {
                list.map {
                    async {
                        log.info("prepare shutdown $it")
                        it.close()
                        it.quit()
                        log.info("shutdown $it success")
                    }
                }.awaitAll()
                queue.close()
                list.clear()
                log.info("shutdown driver-queue success")
            }
        })
    }


    suspend fun useDriver(block:suspend (EdgeDriver) -> Unit) {
        val driver = queue.receive()
        block(driver)
        queue.send(driver)
    }
}
