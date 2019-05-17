package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModException
import cn.tursom.treediagram.modinterface.ModPath
import cn.tursom.treediagram.modinterface.NeedBody
import cn.tursom.treediagram.token.token
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.net.URLDecoder

/**
 * 文件上传模组
 * 需要提供两个参数：
 * filename要上传的文件名称
 * file或者file64
 * file与file64的去别在于file是文本文件的原文件内容，file64是base64编码后的文件内容
 * 返回的是上传到服务器的目录
 */
@ModPath("upload/:type/:filename", "upload/:filename", "upload")
@NeedBody(10 * 1024 * 1024)
class Upload : BaseMod() {

    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        val token = request.token!!

        //确保上传用目录可用
        val uploadPath = getUploadPath(token.usr!!)
        if (!File(uploadPath).exists()) {
            File(uploadPath).mkdirs()
        }

        val filename = URLDecoder.decode(
            request.getParam("filename")
                ?: request.getHeader("filename")
                ?: throw ModException("filename not found")
            , "UTF-8"
        )
        val file = File("$uploadPath$filename")
        val outputStream = when (val uploadType = request.getParam("type")
            ?: request.getHeader("type")
            ?: "append") {
            "create" -> {
                if (file.exists()) throw ModException("file exist")
                else FileOutputStream(file)
            }
            "append" -> {
                FileOutputStream(file, true)
            }
            "delete" -> {
                file.delete()
                return "file \"$filename\" deleted"
            }
            else -> throw ModException(
                "unsupported upload type $uploadType, " +
                        "please use \"create\" or \"append\"(default) as an upload type"
            )
        }

        //写入文件
        outputStream.write(context.body.bytes)

        outputStream.flush()
        outputStream.close()

        response.putHeader("filename", filename)
        //返回上传的文件名
        return filename
    }

    companion object {
        //        @JvmStatic
//        val uploadRootPath = "${MultipleUpload::class.java.getResource("/").path!!}upload/"
        @JvmStatic
        val uploadRootPath = "upload/"

        @JvmStatic
        fun getUploadPath(user: String) = "$uploadRootPath$user/"
    }
}