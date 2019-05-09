package cn.tursom.treediagram.basemod

import cn.tursom.treediagram.modinterface.BaseMod
import cn.tursom.treediagram.modinterface.ModPath
import cn.tursom.treediagram.modinterface.NoBlocking
import cn.tursom.treediagram.token.getToken
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.io.Serializable

/**
 * 获取上传的文件的列表
 */
@NoBlocking
@ModPath("UploadFileList")
class GetUploadFileList : BaseMod() {
    override fun handle(
        context: RoutingContext,
        request: HttpServerRequest,
        response: HttpServerResponse
    ): Serializable? {
        val token = request.getToken()!!
        val uploadPath = "${Upload.uploadRootPath}${token.usr}/"
        val uploadDir = File(uploadPath)
        val fileList = ArrayList<String>()
        uploadDir.listFiles()?.forEach {
            fileList.add(it.path.split('/').last())
        }
        return fileList
    }
}
