package com.luckyaf.wifly.constant

import android.os.Environment
import com.luckyaf.kommon.extension.getFromCache
import com.luckyaf.kommon.extension.saveToCache
import java.io.File

/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-21
 *
 */
object Constants {




    val EVENT_UPLOAD_FILE_LIST = UpdateFileEvent()


    private const val KEY_APP_SERVER_PORT = "app_server_port"
    private const val KEY_APP_SERVER_DIR = "app_server_dir"
    private const val KEY_APP_ALLOW_DELETE = "app_allow_delete"
    private const val KEY_APP_ALLOW_UPLOAD = "app_allow_upload"

    private val DefaultServerDir =
        Environment.getExternalStorageDirectory().toString() + File.separator + "WiFly"


    var serverDir: File
    var serverPort:Int
    var deletable:Boolean
    var uploadAble :Boolean


    init {
        serverPort = getFromCache(KEY_APP_SERVER_PORT,12345)
        val path = getFromCache(KEY_APP_SERVER_DIR, DefaultServerDir)
        serverDir = File(path)
        deletable = getFromCache(KEY_APP_ALLOW_DELETE,true)
        uploadAble = getFromCache(KEY_APP_ALLOW_UPLOAD,true)
    }



    fun updateServerDir(newDir: String) {
        newDir.saveToCache(KEY_APP_SERVER_DIR)
        serverDir = File(newDir)
    }

    fun updateServerPort(newPort:Int){
        newPort.saveToCache(KEY_APP_SERVER_PORT)
        serverPort = newPort
    }
    fun updateAllowDelete(able:Boolean){
        able.saveToCache(KEY_APP_ALLOW_DELETE)
        deletable = able
    }
    fun updateAloowUpload(able:Boolean){
        able.saveToCache(KEY_APP_ALLOW_UPLOAD)
        uploadAble = able
    }

}