package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModException
import cn.tursom.treediagram.modinterface.ModPath
import cn.tursom.treediagram.modloader.ClassData
import cn.tursom.treediagram.token.token
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
@ModPath("loadmod/:jarPath/:className", "loadmod/:modData", "loadmod")
class ModLoader : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        val token = request.token!!
        val modData = request["modData"]
        println(modData)
        val modLoader = if (modData != null) {
            cn.tursom.treediagram.modloader.ModLoader(
                modData.urlDecode,
                if (request["system"] != "true") {
                    token.usr
                } else {
                    null
                },
                Upload.getUploadPath(token.usr!!),
                false,
                router!!
            )
        } else {
            cn.tursom.treediagram.modloader.ModLoader(
                ClassData(
                    null,
                    request["jarPath"]?.urlDecode ?: throw ModException("no mod get"),
                    arrayOf(request["className"]?.urlDecode ?: throw ModException("no mod get"))
                ),
                if (request["system"] != "true") {
                    token.usr
                } else {
                    null
                },
                Upload.getUploadPath(token.usr!!),
                false,
                router!!
            )
        }
        if (!modLoader.load()) throw ModException("mod load error")
        return null
    }
}