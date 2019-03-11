package com.luckyaf.wifly.model

import android.os.BadParcelableException
import android.os.Parcel
import java.io.*

/**
 * 类描述：持久化Util
 * @author Created by luckyAF on 2019-02-27
 *
 */
object DurableUtils {
    @Throws(IOException::class)
    fun <D : Durable> writeToArray(d: D): ByteArray {
        val out = ByteArrayOutputStream()
        d.write(DataOutputStream(out))
        return out.toByteArray()
    }
    @Throws(IOException::class)
    fun <D : Durable> readFromArray(data: ByteArray?, d: D): D {
        if (data == null){
            throw IOException("Missing data")
        }
        val inputStream = ByteArrayInputStream(data)
        d.reset()
        try {
            d.read(DataInputStream(inputStream))
        } catch (e: IOException) {
            d.reset()
            throw e
        }
        return d
    }

    fun <D : Durable> writeToArrayOrNull(d: D): ByteArray? {
        try {
            return writeToArray(d)
        } catch (e: IOException) {
            return null
        }
    }
    fun <D : Durable> readFromArrayOrNull(data: ByteArray?, d: D): D? {
        try {
            return readFromArray(data, d)
        } catch (e: IOException) {
            return null
        }
    }

    fun <D : Durable> writeToParcel(parcel: Parcel, d: D) {
        try {
            parcel.writeByteArray(writeToArray(d))
        } catch (e: IOException) {
            throw BadParcelableException(e)
        }
    }

    fun <D : Durable> readFromParcel(parcel: Parcel, d: D): D {
        try {
            return readFromArray(parcel.createByteArray(), d)
        } catch (e: IOException) {
            throw BadParcelableException(e)
        }

    }

    @Throws(IOException::class)
    fun writeNullableString(out: DataOutputStream, value: String?) {
        if (value != null) {
            out.write(1)
            out.writeUTF(value)
        } else {
            out.write(0)
        }
    }

    @Throws(IOException::class)
    fun readNullableString(inputStream: DataInputStream): String? {
        return if (inputStream.read() != 0) {
            inputStream.readUTF()
        } else {
            null
        }
    }
}