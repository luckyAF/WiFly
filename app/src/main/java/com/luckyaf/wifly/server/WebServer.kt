package com.luckyaf.wifly.server

import android.content.Context
import android.text.TextUtils
import com.koushikdutta.async.AsyncServer
import com.koushikdutta.async.ByteBufferList
import com.koushikdutta.async.DataEmitter
import com.koushikdutta.async.http.body.MultipartFormDataBody
import com.koushikdutta.async.http.body.Part
import com.koushikdutta.async.http.body.UrlEncodedFormBody
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.koushikdutta.async.http.server.AsyncHttpServerRequest
import com.koushikdutta.async.http.server.AsyncHttpServerResponse
import com.luckyaf.kommon.component.RxBus
import com.luckyaf.kommon.extension.DEBUG
import com.luckyaf.kommon.extension.INFO
import com.luckyaf.kommon.utils.Logger
import com.luckyaf.wifly.constant.Constants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.DecimalFormat


/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-21
 *
 */
class WebServer private constructor(val mContext: Context) {

    companion object {
        private const val TEXT_CONTENT_TYPE = "text/html;charset=utf-8"
        private const val CSS_CONTENT_TYPE = "text/css;charset=utf-8"
        private const val BINARY_CONTENT_TYPE = "application/octet-stream"
        private const val JS_CONTENT_TYPE = "application/javascript"
        private const val PNG_CONTENT_TYPE = "application/x-png"
        private const val JPG_CONTENT_TYPE = "application/jpeg"
        private const val SWF_CONTENT_TYPE = "application/x-shockwave-flash"
        private const val WOFF_CONTENT_TYPE = "application/x-font-woff"
        private const val TTF_CONTENT_TYPE = "application/x-font-truetype"
        private const val SVG_CONTENT_TYPE = "image/svg+xml"
        private const val EOT_CONTENT_TYPE = "image/vnd.ms-fontobject"
        private const val MP3_CONTENT_TYPE = "audio/mp3"
        private const val MP4_CONTENT_TYPE = "video/mpeg4"

        fun getInstance(context: Context): WebServer {
            val instance = WebServer(context)
            //instance.prepare()
            return instance
        }
    }


    private var mAsyncHttpServer: AsyncHttpServer? = null
    private var mAsyncServer: AsyncServer? = null
    private var fileUploadWork = FileUploadWork()
    private var fileUploadHolder = FileUploadHolder()
    var fileSize: Long = 0

    fun prepare() {
        mAsyncHttpServer = AsyncHttpServer()
        mAsyncServer = AsyncServer()
        mAsyncHttpServer?.let {
            it.get("/images/.*") { request, response ->
                sendResources(request, response)
            }
            it.get("/scripts/.*") { request, response ->
                sendResources(request, response)
            }
            it.get("/css/.*") { request, response ->
                sendResources(request, response)
            }
            // index page
            it.get("/") { request, response ->
                loadIndex(request, response)
            }
            // 获取文件列表
            it.get("/files") { request, response ->
                queryFiles(request, response)
            }
            // 删除文件
            it.post("/files/.*") { request, response ->
                deleteFile(request, response)
            }
            //上传文件
            it.post("/files"){request, response ->
                uploadFile(request,response)
            }
            // 下载文件
            it.get("/files/.*") { request, response ->
                downloadFile(request,response)
            }
            // 进度
            it.get("/progress/.*"){request, response ->
                getProgress(request,response)
            }
            // 权限
            it.get("/permission"){request, response ->
                getPermission(request,response)
            }

        }

    }


    fun run() {
        prepare()
        mAsyncHttpServer?.listen(mAsyncServer, Constants.serverPort)
    }


    fun stop() {
        mAsyncHttpServer?.stop()
        mAsyncServer?.stop()

    }

    /**
     * 加载资源
     */
    private fun sendResources(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        try {
            var fullPath = request.path
            fullPath = fullPath.replace("%20", " ")
            var resourceName = fullPath
            if (resourceName.startsWith("/")) {
                resourceName = resourceName.substring(1)
            }
            if (resourceName.indexOf("?") > 0) {
                resourceName = resourceName.substring(0, resourceName.indexOf("?"))
            }
            if (!TextUtils.isEmpty(getContentTypeByResourceName(resourceName))) {
                response.setContentType(getContentTypeByResourceName(resourceName))
            }
            val bInputStream = BufferedInputStream(mContext.assets.open("web/$resourceName"))
            response.sendStream(bInputStream, bInputStream.available().toLong())
        } catch (e: IOException) {
            e.printStackTrace()
            response.code(404).end()
            return
        }

    }

    /**
     * 加载主页面
     */
    private fun loadIndex(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        try {
            response.send(getIndexContent())
        } catch (e: IOException) {
            e.printStackTrace()
            response.code(500).end()
        }

    }


    /**
     * 获取文件列表
     */
    private fun queryFiles(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val array = JSONArray()
        val dir = File(Constants.serverDir)
        if (dir.exists() && dir.isDirectory) {
            val fileNames = dir.list()
            fileNames?.map {
                val file = File(dir, it)
                if (file.exists() && file.isFile) {
                    try {
                        val jsonObject = JSONObject()
                        jsonObject.put("name", it)
                        val fileLen = file.length()
                        val df = DecimalFormat("0.00")
                        if (fileLen > 1024 * 1024) {
                            jsonObject.put(
                                "size",
                                "${df.format((fileLen * 1f / 1024f / 1024f).toDouble())}MB"
                            )
                        } else if (fileLen > 1024) {
                            jsonObject.put(
                                "size",
                                "${df.format((fileLen * 1f / 1024).toDouble())}KB"
                            )
                        } else {
                            jsonObject.put("size", "${fileLen}B")
                        }
                        array.put(jsonObject)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

        }
        response.send(array.toString())
    }


    /**
     * 删除文件
     */
    private fun deleteFile(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val body = request.body as UrlEncodedFormBody
        if ("delete".equals(body.get().getString("_method"), ignoreCase = true)) {
            var path = request.path.replace("/files/", "")
            try {
                path = URLDecoder.decode(path, "utf-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            val file = File(Constants.serverDir, path)
            if (file.exists() && file.isFile) {
                file.delete()
                RxBus.post(Constants.EVENT_UPLOAD_FILE_LIST)

                response.end()
            }
        }
    }


    /**
     * 上传文件
     */
    private fun uploadFile(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        val body = request.body as MultipartFormDataBody
        if (!Constants.uploadAble) {
            response.code(405).send("upload not allowed!")
            return
        }
        body.setMultipartCallback { part: Part ->
            "part name=${part.name} length=${part.length()}".DEBUG()
            if (part.isFile) {
                body.setDataCallback { emitter: DataEmitter, bb: ByteBufferList ->
                    val data = bb.allByteArray
                   // fileUploadWork.post(data)
                    fileUploadHolder.write(data)
                    fileSize += data.size
                    bb.recycle()
                }
            } else {
                if (body.dataCallback == null) {
                    body.setDataCallback { emitter: DataEmitter, bb: ByteBufferList ->
                        try {
                            val fileName = URLDecoder.decode(String(bb.allByteArray), "utf-8")
//                            fileUploadWork.init(fileName)
//                            fileUploadWork.start()
                            fileUploadHolder.init(fileName)
                            fileSize = 0
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }

                        bb.recycle()
                    }
                }
            }

        }

        request.setEndCallback {
            "body.setEndCallback".INFO()
            //fileUploadWork.finishWork()
            "fileSize = $fileSize".INFO()
            "totalSize = ${fileUploadHolder.totalSize}".INFO()
            "request endCallback".INFO()
            fileUploadHolder.reset()
            response.end()
            RxBus.post(Constants.EVENT_UPLOAD_FILE_LIST)
        }

    }




    /**
     * 下载文件
     */
    private fun downloadFile(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse) {
        var path = request.path.replace("/files/", "")
        try {
            path = URLDecoder.decode(path, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val file = File(Constants.serverDir, path)
        if (file.exists() && file.isFile) {
            try {
                response.headers.add(
                    "Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(file.getName(), "utf-8")
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            response.sendFile(file)
            return
        }
        response.code(404).send("Not found!")
    }


    private fun getProgress(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse){
        val res = JSONObject()
        val path = request.path.replace("/progress/", "")
        if (path == fileUploadWork.fileName) {
            try {
                res.put("fileName", fileUploadWork.fileName)
                res.put("size", fileUploadWork.totalSize)
                res.put("progress", if (fileUploadWork.fileOutPutStream == null) 1 else 0.1)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        response.send(res)
    }


    private fun getPermission(request: AsyncHttpServerRequest, response: AsyncHttpServerResponse){
        val res = JSONObject()
        res.put("deletable", Constants.deletable)
        res.put("uploadAble", Constants.uploadAble)
        response.send(res)
    }

    @Throws(IOException::class)
    private fun getIndexContent(): String {
        var bInputStream: BufferedInputStream? = null
        try {
            bInputStream = BufferedInputStream(mContext.assets.open("web/index.html"))
            val baos = ByteArrayOutputStream()
            val tmp = ByteArray(10240)
            var len = bInputStream.read(tmp)
            while ((len) > 0) {
                baos.write(tmp, 0, len)
                len = bInputStream.read(tmp)
            }
            return String(baos.toByteArray(), Charsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        } finally {
            if (bInputStream != null) {
                try {
                    bInputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }


    private fun getContentTypeByResourceName(resourceName: String): String {
        if (resourceName.endsWith(".css")) {
            return CSS_CONTENT_TYPE
        } else if (resourceName.endsWith(".js")) {
            return JS_CONTENT_TYPE
        } else if (resourceName.endsWith(".swf")) {
            return SWF_CONTENT_TYPE
        } else if (resourceName.endsWith(".png")) {
            return PNG_CONTENT_TYPE
        } else if (resourceName.endsWith(".jpg") || resourceName.endsWith(".jpeg")) {
            return JPG_CONTENT_TYPE
        } else if (resourceName.endsWith(".woff")) {
            return WOFF_CONTENT_TYPE
        } else if (resourceName.endsWith(".ttf")) {
            return TTF_CONTENT_TYPE
        } else if (resourceName.endsWith(".svg")) {
            return SVG_CONTENT_TYPE
        } else if (resourceName.endsWith(".eot")) {
            return EOT_CONTENT_TYPE
        } else if (resourceName.endsWith(".mp3")) {
            return MP3_CONTENT_TYPE
        } else if (resourceName.endsWith(".mp4")) {
            return MP4_CONTENT_TYPE
        }
        return ""
    }


}