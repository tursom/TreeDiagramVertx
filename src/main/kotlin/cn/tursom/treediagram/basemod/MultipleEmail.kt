package cn.tursom.treediagram.basemod

import cn.tursom.tools.fromJson
import cn.tursom.treediagram.datastruct.MultipleEmailData
import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.token.getToken
import com.google.gson.Gson
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.Serializable


class MultipleEmail : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        request.getToken()!!
        try {
            val groupEmailData = gson.fromJson<MultipleEmailData>(request["message"]!!)
            groupEmailData.send()
        } catch (e: Exception) {
            return "${e::class.java}: ${e.message}"
        }
        return "true"
    }

    companion object {
        private val gson = Gson()
    }
}
