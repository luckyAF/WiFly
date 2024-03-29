package com.luckyaf.wifly.server

import com.luckyaf.kommon.component.RxBus
import com.luckyaf.wifly.constant.Constants
import io.reactivex.FlowableSubscriber
import io.reactivex.disposables.CompositeDisposable
import org.reactivestreams.Subscription
import java.io.*

/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-25
 *
 */
class FileUploadHolder {
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

    fun write(data: ByteArray) {
        if (fileOutPutStream != null) {
            try {
                fileOutPutStream?.write(data)
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        totalSize += data.size.toLong()
    }
}