package top.kagg886.cctr.backend.task

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import top.kagg886.cctr.api.CCTRUser
import top.kagg886.cctr.api.modules.QueryModel
import top.kagg886.cctr.api.modules.queryQuestionList
import top.kagg886.cctr.backend.dao.Task
import top.kagg886.cctr.backend.dao.Tasks
import top.kagg886.cctr.backend.dao.Tasks.ExportType.IMG
import top.kagg886.cctr.backend.dao.Tasks.ExportType.PDF
import top.kagg886.cctr.backend.service.TaskService
import top.kagg886.cctr.backend.util.error
import top.kagg886.cctr.backend.util.info
import top.kagg886.cctr.desktop.trayChannel
import top.kagg886.cctr.desktop.util.convertToPDF
import top.kagg886.cctr.desktop.util.root_file
import top.kagg886.cctr.driver.WebDriverDispatcher
import top.kagg886.cctr.driver.captchaImage
import top.kagg886.cctr.util.mergeVertical
import top.kagg886.cctr.util.useTempDictionary
import top.kagg886.cctr.util.useTempDictionarySuspend
import top.kagg886.cctr.util.zip
import java.awt.image.BufferedImage
import java.time.LocalDateTime
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.reflect.jvm.jvmName

private val divider = BufferedImage(10, 50, BufferedImage.TYPE_INT_ARGB)

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
                task.config.filter { (_, map) -> map.isNotEmpty() }.map { (pr, map) ->
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
            val all = questions.toMap().flatMap { it.value.flatMap { it1 -> it1.value } }.size
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
                        if (k == 0) {
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
                                                if (option.isTrue) {
                                                    i2.add(
                                                        driver.captchaImage(
                                                            """
                                                        <div style="padding-left: 20px;padding-top: 10px;display: flex;align-items: baseline;color: red">
                                                            <span>√</span>
                                                            ${option.html.trim()}
                                                        </div>
                                                    """.trimIndent()
                                                        )
                                                    )
                                                } else {
                                                    i2.add(
                                                        driver.captchaImage(
                                                            """
                                                        <div style="padding-left: 20px;padding-top: 10px;">
                                                            ${option.html}
                                                        </div>
                                                    """.trimIndent()
                                                        )
                                                    )
                                                }
                                            }
                                        } else {
                                            i2.add(
                                                driver.captchaImage(
                                                    """
                                                        <div style="padding: 30px">
                                                        <span>答：</span>
                                                        ${it.answer}
                                                        </div>
                                                    """.trimIndent()
                                                )
                                            )
                                        }
                                        i2.add(divider)
                                        val img = i2.mergeVertical()
                                        val file =
                                            root.resolve(pr.practiceName).resolve(cType.name).resolve(it.questionType)
                                                .resolve(it.id + ".png")
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
                        //root/练习名/章节名/题型名/图片
                        useTempDictionary { tmp ->
                            for (prFile in root.listFiles()!!) { //练习名
                                for (cpFile in prFile.listFiles()!!) { //章节名
                                    for (qFile in cpFile.listFiles()!!) { //题型名
                                        qFile.listFiles()!!.toList().convertToPDF(
                                            tmp.resolve(prFile.name).resolve(cpFile.name).resolve(qFile.name + ".pdf")
                                                .apply {
                                                    parentFile.mkdirs()
                                                    createNewFile()
                                                }
                                        )
                                    }
                                }
                            }
                            tmp.zip(
                                out = root_file.resolve("out").resolve("task_" + task.id.toString() + ".zip")
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

                    IMG -> {
                        root.zip(
                            out = root_file.resolve("out").resolve("task_" + task.id.toString() + ".zip")
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
            trayChannel.send("任务id:${task.id}执行失败，原因请打开对应任务日志查看")
            return@launch
        }
        check(TaskService.update(task.copy(status = Tasks.TaskStatus.SUCCESS)))
        task.info("任务记录更新成功")
        trayChannel.send("任务id:${task.id}执行成功")
    }
}