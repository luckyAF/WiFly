package com.luckyaf.wifly.model

import java.text.SimpleDateFormat
import java.util.*


/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-26
 *
 */
data class FileModel(
    var name: String = "",
    var path: String = "",
    var size: Long = 0,
    var lastModified: Long = 0
){
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
    fun getFileSize():String{
        return when{
            size<1000 -> "${size}B"
            size<1000000 -> "${size/1000}KB"
            size<1000000000 -> "${size/1000000}MB"
            else -> "好大只啊"
        }
    }

    fun getLastModifiedTime():String{
        return dateFormat.format(Date(lastModified))
    }
}