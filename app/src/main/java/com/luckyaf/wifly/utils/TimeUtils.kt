package com.luckyaf.wifly.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 类描述：
 * @author Created by luckyAF on 2019-03-12
 *
 */
object TimeUtils {
    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    fun formatYYYYMMddHHmm(time:Date):String{
        return  simpleDateFormat.format(time)
    }
}