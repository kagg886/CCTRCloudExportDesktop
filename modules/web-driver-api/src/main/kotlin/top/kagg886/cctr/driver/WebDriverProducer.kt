package top.kagg886.cctr.driver

import org.openqa.selenium.edge.EdgeDriver
import org.openqa.selenium.edge.EdgeDriverService
import org.openqa.selenium.edge.EdgeOptions
import java.io.File

internal object WebDriverProducer {
    private var init = false
    private lateinit var root: File


    fun newHeadlessDriver(): EdgeDriver {
        check(init) {
            "please call init()"
        }
        return EdgeDriver(EdgeDriverService.createDefaultService(), object : EdgeOptions() {
            init {
                addArguments("headless")
                addArguments("--disable-gpu")
                addArguments("lang=lang=zh_CN.UTF-8")
                setBinary(File(root, "chromedriver"))
            }
        })
    }

    fun init(edgeDriver: File) {
        check(!init) {
            "please don't call init() again"
        }
        root = edgeDriver

        init = true
    }
}