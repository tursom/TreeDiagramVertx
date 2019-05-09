package cn.tursom.treediagram.modinterface

import cn.tursom.treediagram.ReturnData
import cn.tursom.treediagram.gson
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.io.Serializable

@NoBlocking
abstract class BaseMod : Handler<RoutingContext> {
    /**
     * 模组私有目录
     * 在调用的时候会自动创建目录，不必担心目录不存在的问题
     * 如果有模组想储存文件请尽量使用这个目录
     */
    val modPath by lazy {
        val path = "${BaseMod::class.java.getResource("/").path!!}${this::class.java.name}/"
        val dir = File(path)
        if (!dir.exists()) dir.mkdirs()
        path
    }

    /**
     * 当模组被初始化时被调用
     */
    open fun init() {}

    /**
     * 处理模组调用请求
     * @param token 解析过后的用户token
     * @param request 用户通过get或者post提交的数据
     * @return 一个用于表示json数据的对象或者null
     */
    abstract fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable?

    override fun handle(context: RoutingContext) {
        val response = context.response()
        val ret = ReturnData(
            true, try {
                val request = context.request()
                handle(context, request, response)
            } catch (e: Throwable) {
                if (e.message != null)
                    "${e.javaClass}: ${e.message}"
                else
                    e.javaClass
            }
        )
        response.end(gson.toJson(ret)!!)
    }

    /**
     * 当模组生命周期结束时被调用
     */
    open fun destroy() {}

    /**
     * 方便获取ServletRequest里面的数据
     * 使得子类中可以直接使用request[ 参数名 ]的形式来获取数据
     */
    operator fun HttpServerRequest.get(key: String): String? = (this.getHeader(key) ?: this.getParam(key))
}

val BaseMod.modName
    get() = this.javaClass.name

val BaseMod.simpName
    get() = modName.split(".").last()