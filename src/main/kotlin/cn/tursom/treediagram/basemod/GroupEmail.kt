package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.datastruct.GroupEmailData
import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.token.token
import com.google.gson.Gson
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.Serializable

/**
 * 用于群发邮件的模组
 * 为每个人发送内容相同的邮件
 */
class GroupEmail : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        request.token!!
        try {
            val groupEmailData = GroupEmailData(
                request["host"],
                request["port"]?.toInt(),
                request["name"],
                request["password"],
                request["from"],
                gson.fromJson(request["to"], Array<String>::class.java),
                request["subject"],
                request["html"],
                request["text"],
                gson.fromJson(request["image"], Image::class.java),
                gson.fromJson(request["attachment"], Array<String>::class.java)
            )
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
