package com.luckyaf.wifly.server

import com.luckyaf.wifly.constant.Constants
import com.luckyaf.wifly.utils.FlowableWork
import java.io.*

/**
 * 类描述：
 * @author Created by luckyAF on 2019-03-13
 *
 */
class FileUploadWork :FlowableWork<ByteArray>(){
    var fileName: String? = null
    var receivedFile: File? = null
    var fileOutPutStream: BufferedOutputStream? = null
    var totalSize: Long = 0

    fun init(name: String) {
        this.fileName = name
        totalSize = 0
        val file = File(Constants.serverDir)
        if (!file.exists()) {
            file.mkdirs()
        }
        this.receivedFile = File(Constants.serverDir, this.fileName)
        try {
            fileOutPutStream = BufferedOutputStream(FileOutputStream(receivedFile))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

    }

    override fun finishWork() {
        super.finishWork()
        reset()
    }

    fun reset() {
        if (fileOutPutStream != null) {
            try {
                fileOutPutStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        fileOutPutStream = null
    }

    private fun write(data: ByteArray) {
        if (fileOutPutStream != null) {
            try {
                fileOutPutStream!!.write(data)

            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        totalSize += data.size.toLong()
    }


    override fun handleData(data: ByteArray) {
        write(data)
    }
}