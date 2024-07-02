package top.kagg886.cctr.driver

import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions
import java.io.File

internal object WebDriverProducer {
    var init = false
    private lateinit var driverFile: File
    private lateinit var executableFile: File
    private var headless: Boolean = true


    fun newHeadlessDriver(): EdgeDriver {
        check(init) {
            "please call init()"
        }
        return EdgeDriver(EdgeDriverService.Builder().usingDriverExecutable(driverFile).usingAnyFreePort().build(),
            object : EdgeOptions() {
                init {
                    if (headless) {
                        addArguments("headless")
                    }
                    addArguments("--disable-gpu")
                    addArguments("lang=lang=zh_CN.UTF-8")
                    setBinary(executableFile)
                }
            })
    }

    fun init(driverFile: File, edgeExecutable: File,headless:Boolean = true) {
        check(!init) {
            "please don't call init() again"
        }
        this@WebDriverProducer.driverFile = driverFile
        this@WebDriverProducer.executableFile = edgeExecutable
        this@WebDriverProducer.headless = headless
        init = true
    }
}