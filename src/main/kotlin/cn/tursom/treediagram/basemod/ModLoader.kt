package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModException
import cn.tursom.treediagram.modinterface.ModPath
import cn.tursom.treediagram.token.TokenData
import cn.tursom.treediagram.token.findUser
import cn.tursom.treediagram.token.getToken
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.Serializable

/**
 * 模组加载模组
 * 用于加载一个模组
 *
 * 需要提供的参数有：
 * mod 要加载的模组的信息，结构为json序列化后的ClassData数据
 * system 可选，是否加入系统模组，需要admin权限
 *
 * 本模组会根据提供的信息自动寻找模组并加载
 * 模组加载的根目录为使用Upload上传的根目录
 */
@ModPath("loadmod", "loadmod/:modData")
class ModLoader : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        val token = request.getToken()!!
        val modData = request["modData"]
        println(modData)
        val modLoader = if (request["system"] != "true") {
            cn.tursom.treediagram.modloader.ModLoader(
                modData ?: throw ModException("no mod get"),
                token.usr,
                Upload.getUploadPath(token.usr!!),
                false,
                router!!
            )
        } else {
            if (findUser(token.usr!!)?.level != "admin") throw ModException("user not admin")
            cn.tursom.treediagram.modloader.ModLoader(
                request["modData"] ?: throw ModException("no mod get"),
                null,
                Upload.getUploadPath(token.usr),
                false,
                router!!
            )
        }
        if (!modLoader.load()) throw ModException("mod load error")
        return null
    }
}