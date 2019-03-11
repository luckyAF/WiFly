package com.luckyaf.wifly.utils

import android.content.Context
import android.os.Environment
import java.io.File
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.util.*
import android.support.v4.content.FileProvider
import android.os.Build
import com.luckyaf.wifly.model.FileModel


/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-21
 *
 */
object FileUtils {
    private val DATA_TYPE_ALL = "*/*"//未指定明确的文件类型，不能使用精确类型的工具打开，需要用户选择
    private val DATA_TYPE_APK = "application/vnd.android.package-archive"
    private val DATA_TYPE_VIDEO = "video/*"
    private val DATA_TYPE_AUDIO = "audio/*"
    private val DATA_TYPE_HTML = "text/html"
    private val DATA_TYPE_IMAGE = "image/*"
    private val DATA_TYPE_PPT = "application/vnd.ms-powerpoint"
    private val DATA_TYPE_EXCEL = "application/vnd.ms-excel"
    private val DATA_TYPE_WORD = "application/msword"
    private val DATA_TYPE_CHM = "application/x-chm"
    private val DATA_TYPE_TXT = "text/plain"
    private val DATA_TYPE_PDF = "application/pdf"

    // 获取指定目录下的文件与子目录

    fun getFilesByDir(dirString: String?): List<FileModel>? {
        val parentFile: File
        val fileModels = ArrayList<FileModel>()
        parentFile = if (dirString.isNullOrBlank()) {
             Environment.getRootDirectory()
        } else {
            File(dirString)
        }
        val fileList = parentFile.listFiles()
        if (fileList.isNullOrEmpty()) {
            return null
        }
        for (file in fileList) {
            val fileModel = FileModel()
            if (file.isDirectory) {
                // 默认不展示文件夹
                continue
            }
            fileModel.name = file.name
            fileModel.path = file.path
            fileModel.size = file.length()
            fileModel.lastModified = file.lastModified()
            fileModels.add(fileModel)
        }
        return fileModels
    }

    /**
     * 打开文件
     * @param filePath 文件的全路径，包括到文件名
     */
    public fun openFile(context: Context,filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            //如果文件不存在
            Toast.makeText(context, "打开失败，原因：文件已经被移动或者删除", Toast.LENGTH_SHORT).show()
            return
        }
        /* 取得扩展名 */
        val end = file.name.substring(file.name.lastIndexOf(".") + 1, file.name.length)
            .toLowerCase(Locale.getDefault())
        /* 依扩展名的类型决定MimeType */
        var intent: Intent? = null
        if (end == "m4a" || end == "mp3" || end == "mid" || end == "xmf" || end == "ogg" || end == "wav") {
            intent = generateVideoAudioIntent(context,filePath, DATA_TYPE_AUDIO)
        } else if (end == "3gp" || end == "mp4") {
            intent = generateVideoAudioIntent(context,filePath, DATA_TYPE_VIDEO)
        } else if (end == "jpg" || end == "gif" || end == "png" || end == "jpeg" || end == "bmp") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_IMAGE)
        } else if (end == "apk") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_APK)
        } else if (end == "html" || end == "htm") {
            intent = getHtmlFileIntent(context,filePath)
        } else if (end == "ppt") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_PPT)
        } else if (end == "xls") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_EXCEL)
        } else if (end == "doc") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_WORD)
        } else if (end == "pdf") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_PDF)
        } else if (end == "chm") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_CHM)
        } else if (end == "txt") {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_TXT)
        } else {
            intent = generateCommonIntent(context,filePath, DATA_TYPE_ALL)
        }
        context.startActivity(intent)
    }

    /**
     * 产生打开视频或音频的Intent
     * @param filePath 文件路径
     * @param dataType 文件类型
     * @return
     */
    private fun generateVideoAudioIntent(context: Context,filePath: String, dataType: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("oneshot", 0)
        intent.putExtra("configchange", 0)
        val file = File(filePath)
        intent.setDataAndType(getUri(context,intent, file), dataType)
        return intent
    }

    // Android获取一个用于打开Html文件的intent
    fun getHtmlFileIntent(context: Context,param: String): Intent {
        val uri = Uri.parse(param).buildUpon().encodedAuthority(context.packageName+ ".fileprovider")
            .scheme("content").encodedPath(param).build()
        val intent = Intent("android.intent.action.VIEW")
        intent.setDataAndType(uri, "text/html")
        return intent
    }

    /**
     * 产生除了视频、音频、网页文件外，打开其他类型文件的Intent
     * @param filePath 文件路径
     * @param dataType 文件类型
     * @return
     */
    private fun generateCommonIntent(context: Context,filePath: String, dataType: String): Intent {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        val file = File(filePath)
        val uri = getUri(context,intent, file)
        intent.setDataAndType(uri, dataType)
        return intent
    }

    /**
     * 获取对应文件的Uri
     * @param intent 相应的Intent
     * @param file 文件对象
     * @return
     */
    private fun getUri(context: Context,intent: Intent, file: File): Uri? {
        var uri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本是否在7.0以上
            uri = FileProvider.getUriForFile(
                context,
                context.packageName+ ".fileprovider",
                file
            )
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            uri = Uri.fromFile(file)
        }
        return uri
    }


}