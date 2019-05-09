package cn.tursom.treediagram.basemod

import cn.tursom.database.async.vertx
import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModException
import cn.tursom.treediagram.basemod.Upload.Companion.getUploadPath
import cn.tursom.treediagram.modinterface.NeedBody
import cn.tursom.treediagram.token.getToken
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.net.URLDecoder


/**
 * 复数文件上传模组
 * 需要提供参数：
 * filename要上传的文件名称
 * 返回的是上传到服务器的文件名
 */
@NeedBody(10 * 1024 * 1024)
class MultipleUpload : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        val token = request.getToken()!!
        val uploadList = ArrayList<String>()
        //确保上传用目录可用
        val uploadPath = getUploadPath(token.usr!!)
        if (!File(uploadPath).exists()) {
            File(uploadPath).mkdirs()
        }

        //遍历上传的每一个文件
        context.fileUploads().forEach { file ->
            val uploadedFile = vertx.fileSystem().readFileBlocking(file.uploadedFileName())
            val fileName = URLDecoder.decode(file.fileName(), "UTF-8")
            uploadList.add(fileName)

            //建立上传文件，打开文件输出流
            val uploadFile = File("$uploadPath$fileName")
            val outputStream = when (request.getHeader("type") ?: "append") {
                "create" -> {
                    if (uploadFile.exists()) throw ModException("file exist")
                    FileOutputStream(uploadFile)
                }
                "append" -> {
                    FileOutputStream(uploadFile, true)
                }
                else -> throw ModException(
                    "unsupported upload type, " +
                            "please use \"create\" or \"append\"(default) as an upload type"
                )
            }

            //写入文件
            outputStream.write(uploadedFile.bytes)
            outputStream.flush()
            outputStream.close()
        }

        //返回上传的文件列表
        return uploadList
    }
}
