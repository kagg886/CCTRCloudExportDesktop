package top.kagg886.cctr.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import top.kagg886.cctr.api.util.*

data class CCTRUser internal constructor(
    val schoolId:String,
    val userName:String,
    val passWord:String,
) {
    internal lateinit var net: HttpClient

    internal lateinit var key: String

    companion object {
        suspend fun newCCTRUser(block: CCTRUserBuilder.() -> Unit): CCTRUser {
            val config = CCTRUserBuilder().apply(block)
            return CCTRUser(config.schoolId,config.username,config.password).apply {
                init()
            }
        }
    }

    suspend fun init() {
        val client = newHTTPClient()
        val schoolId = kotlin.run {
            val data = client.post("https://api.cctrcloud.net/mobile/index.php?act=login&op=getschoolData") {
                method = HttpMethod.Post
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    buildURLParams(
                        "number" to schoolId
                    )
                )
            }.body<BaseResponse>()

            if (data.code != 200) {
                throw CCTRException(data.data<ErrorMessage>()!!.error)
            }

            @Serializable
            data class A(
                @SerialName("ID")
                val id: String
            )

            data.data<A>()!!.id
        }
        val result = client.post("https://api.cctrcloud.net/mobile/index.php?act=login") {
            method = HttpMethod.Post
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                buildURLParams(
                    "username" to userName,
                    "password" to RSA.encrypt(passWord),
                    "schoolid" to schoolId,
                    "client" to "web"
                )
            )
//                setBody("username=${config.username}&password=${RSA.encrypt(config.password)}&schoolid=${schoolId}&client=web")
        }.body<BaseResponse>()
        if (result.code != 200) {
            throw CCTRException(result.data<ErrorMessage>()!!.error)
        }

        @Serializable
        data class UserInfo(
            val key:String,
        )

        net = client
        key = result.data<UserInfo>()!!.key
    }
}

class CCTRUserBuilder {
    lateinit var schoolId: String
    lateinit var username: String
    lateinit var password: String
}

private fun newHTTPClient(): HttpClient = HttpClient {
    install(HttpCookies) {
        storage = AcceptAllCookiesStorage()
    }

    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.ALL
    }

    install(ContentNegotiation) {
        val json = Json {
            ignoreUnknownKeys = true
        }
        json(json)
//        为错误的Content-Type手动添加序列化
        serialization(ContentType.Text.Html, json)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 60_000
        connectTimeoutMillis = 60_000
        socketTimeoutMillis = 60_000
    }
}

