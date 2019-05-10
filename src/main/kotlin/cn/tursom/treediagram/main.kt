package cn.tursom.treediagram

import cn.tursom.database.async.vertx
import cn.tursom.tools.fromJson
import cn.tursom.tools.sendGet
import cn.tursom.tools.sendPost
import cn.tursom.tools.sha256
import cn.tursom.treediagram.modloader.ModManager
import cn.tursom.treediagram.token.TokenData
import com.google.gson.Gson
import io.vertx.core.Handler
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.net.URLEncoder
import kotlin.system.exitProcess

val gson = Gson()

fun main() {
    val router: Router = Router.router(vertx)
    val server = vertx.createHttpServer()
    ModManager.loadBaseMod(router)
    val handler = Handler<RoutingContext> handler@{ context ->
        try {
            val request = context.request()
            val response = context.response()
            val modName = request.getHeader("mod") ?: request.getParam("mod")!!
            val mod = ModManager.getSystemMod(modName)!!
            val ret = ReturnData(true, mod.handle(context, request, response))
            response.end(gson.toJson(ret)!!)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    router.route("/mod/").blockingHandler(handler)
    router.route("/mod").blockingHandler(handler)
    server.requestHandler(router::handle)
    server.listen(8086)

    router.routes.forEach { println(it.path) }

    try {
        val token = gson.fromJson(
            sendGet(
                "http://127.0.0.1:8086/mod/system/login/tursom",
                headers = mapOf("password" to "test".sha256()!!)
            ), ReturnData::class.java
        ).result as String

        val filename = URLEncoder.encode("海星.txt", "utf-8")

        println(
            sendGet(
                "http://127.0.0.1:8086/mod/system/upload/delete/$filename",
                headers = mapOf("token" to token)
            )
        )

        println(
            sendPost(
                "http://127.0.0.1:8086/mod/system/upload/$filename",
                File("build.gradle").readBytes(),
                headers = mapOf("token" to token)
            )
        )
    } catch (e: Throwable) {
        e.printStackTrace()
    }

    server.close()
    exitProcess(0)
}