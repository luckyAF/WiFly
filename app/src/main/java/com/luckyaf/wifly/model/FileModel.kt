package com.luckyaf.wifly.model

import com.luckyaf.wifly.utils.TimeUtils
import java.io.Serializable
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
):Serializable{

    fun getFileSize():String{
        return when{
            size<1000 -> "${size}B"
            size<1000000 -> "${size/1000}KB"
            size<1000000000 -> "${size/1000000}MB"
            else -> "好大只啊"
        }
    }

    fun getLastModifiedTime():String{
        return TimeUtils.formatYYYYMMddHHmm(Date(lastModified))
    }
}