package top.kagg886.cctr.backend.task

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.*
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.service.TaskService
import top.kagg886.cctr.backend.dao.Tasks
import top.kagg886.cctr.backend.dao.Tasks.ExportType.*
import top.kagg886.cctr.backend.util.error
import top.kagg886.cctr.backend.util.info
import top.kagg886.cctr.driver.WebDriverDispatcher
import top.kagg886.cctr.driver.captchaImage
import top.kagg886.cctr.util.mergeVertical
import top.kagg886.cctr.util.useTempDictionarySuspend
import top.kagg886.cctr.util.zip
import java.awt.image.BufferedImage
import java.io.File
import java.time.LocalDateTime
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.reflect.jvm.jvmName

object TaskManager {

    private val dispatcher = CoroutineScope(
        context = ThreadPoolExecutor(
            2, 5, 60, TimeUnit.SECONDS, ArrayBlockingQueue(1000), ThreadPoolExecutor.AbortPolicy()
        ).asCoroutineDispatcher() + SupervisorJob()
    )

    fun start() {
        dispatcher.launch {
            // 将所有正在进行的任务标记为失败
            TaskService.query {
                where { Tasks.status eq Tasks.TaskStatus.PROCESSING }
            }.forEach {
                it.error("系统重启，任务自动失败")
                TaskService.update(it.copy(status = Tasks.TaskStatus.FAILED))
            }

            //启动所有标记为等待的任务
            TaskService.query {
                where { Tasks.status eq Tasks.TaskStatus.WAITING }.orderBy(Tasks.createTime)
            }.map {
                submitTask(it)
            }
        }
    }

    suspend fun commitTask(vm: Task): Task {
        val dbTask = checkNotNull(
            TaskService.insert(
                Task(
                    id = -1,
                    schoolId = vm.schoolId,
                    username = vm.username,
                    password = vm.password,
                    config = vm.config,
                    status = Tasks.TaskStatus.WAITING,
                    createTime = LocalDateTime.now(),
                    exportType = vm.exportType,
                )
            )
        ) {
            "添加任务失败"
        }
        dbTask.info("成功添加任务记录")
        submitTask(task = dbTask)
        dbTask.info("任务已进入队列")
        return dbTask
    }

    private fun submitTask(task: Task) = dispatcher.launch {
        check(task.id != -1) {
            "请规范化taskId!"
        }
        check(TaskService.update(task.copy(status = Tasks.TaskStatus.PROCESSING))) {
            "修改数据库内任务状态失败!"
        }
        try {
            task.info("任务开始执行")
            task.info("准备登陆")
            val cctr = CCTRUser.newCCTRUser {
                schoolId = task.schoolId
                username = task.username
                password = task.password
            }
            cctr.init()
            task.info("登录成功")

            val questions = buildMap big@{
                task.config.map { (pr, map) ->
                    async(Dispatchers.IO) {
                        this@big[pr] = buildMap small@{
                            map.map { (cType, qType) ->
                                async(Dispatchers.IO) {
                                    val a = cctr.queryQuestionList(pr) {
                                        chapter = listOf(cType)
                                        question = qType
                                    }
                                    val b = cctr.queryQuestionList(pr) {
                                        chapter = listOf(cType)
                                        question = qType
                                        practiceType = QueryModel.PracticeType.PRACTICED
                                    }
                                    val c = a.plus(b)
                                    this@small[cType] = c
                                }
                            }.awaitAll()
                        }
                    }
                }.awaitAll()
            }
            val all = questions.flatMap { it.value.flatMap { it1 -> it1.value } }.size
            task.info("查询需要导出的题目成功，共${all}个")
            val progressChannel = Channel<Int>()
            var progress = 0
            val percentArr = (1..10).map {
                (all / 10) * it
            }
            dispatcher.launch receiver@{
                kotlin.runCatching {
                    while (true) {
                        progressChannel.receive()
                        progress++
                        val k = percentArr.indexOf(progress) + 1
                        if (k == 0){
                            continue
                        }
                        task.info("进度:${k}0%")
                    }
                }
            }
            useTempDictionarySuspend { root ->
                //download image
                questions.map { (pr, map) ->
                    async(Dispatchers.IO) {
                        map.map { (cType, list) ->
                            async(Dispatchers.IO) {
                                list.map {
                                    WebDriverDispatcher.useDriver { driver ->
                                        val i2 = mutableListOf<BufferedImage>().apply {
                                            add(driver.captchaImage(it.subjectHtml))
                                        }

                                        if (it.hasOptions) {
                                            for (option in it.options) {
                                                i2.add(driver.captchaImage(option.html))
                                            }
                                        } else {
                                            i2.add(driver.captchaImage(it.answer))
                                        }
                                        val img = i2.mergeVertical()
                                        val file =
                                            root.resolve(pr.practiceName).resolve(cType.name).resolve(it.id + ".png")
                                                .apply {
                                                    if (!exists()) {
                                                        parentFile.mkdirs()
                                                        createNewFile()
                                                    }
                                                }
                                        ImageIO.write(img, "PNG", file)
                                        progressChannel.send(0)
                                    }
                                }
                            }
                        }.awaitAll()
                    }
                }.awaitAll()

                when (task.exportType) {
                    PDF -> {
                        task.error("暂不支持，请等待更新")
                        throw RuntimeException()
                    }

                    IMG -> {
                        root.zip(
                            out = File("cctr-desktop").resolve("out").resolve("task_" + task.id.toString() + ".zip")
                                .apply {
                                    if (exists()) {
                                        delete()
                                    }
                                    parentFile.mkdirs()
                                    createNewFile()
                                }
                        )
                    }
                }
            }
            progressChannel.close()
        } catch (e: Throwable) {
            e.printStackTrace()
            task.error("任务执行失败:${e::class.jvmName} ${e.message}")
            check(TaskService.update(task.copy(status = Tasks.TaskStatus.FAILED))) {
                "修改数据库内任务状态失败!"
            }
            return@launch
        }
        check(TaskService.update(task.copy(status = Tasks.TaskStatus.SUCCESS)))
        task.info("任务记录更新成功")
    }
}