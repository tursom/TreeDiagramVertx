package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModPath
import cn.tursom.treediagram.token.register
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.runBlocking
import java.io.Serializable

@ModPath("register", "register/:username")
class Register : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        return try {
            runBlocking { register(request) }
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }
    }


    override fun handle(context: RoutingContext) {
        val request = context.request()
        val response = context.response()
        response.end(runBlocking { register(request) })
    }
}