package com.luckyaf.wifly.model

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * 类描述：数据持久化
 * @author Created by luckyAF on 2019-02-27
 *
 */
interface Durable {
    fun reset()
    @Throws(IOException::class)
    fun read(dataInputStream: DataInputStream)
    @Throws(IOException::class)
    fun write(outputStream: DataOutputStream)
}