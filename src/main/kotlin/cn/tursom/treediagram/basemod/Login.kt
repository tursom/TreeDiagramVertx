package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModPath
import cn.tursom.treediagram.token.login
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.runBlocking
import java.io.Serializable

@ModPath("login", "login/:name")
class Login : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        val username = request.getParam("name")
        val password = request.getParam("password")
        return runBlocking { login(username, password) }
    }

    override fun handle(context: RoutingContext) {
        val request = context.request()
        val response = context.response()
        val username = request.getHeader("name") ?: request.getParam("name")
        val password = request.getHeader("password") ?: request.getParam("password")
        response.end(runBlocking { login(username, password) })
    }
}