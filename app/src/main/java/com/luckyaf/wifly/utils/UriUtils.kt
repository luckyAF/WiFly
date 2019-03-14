package com.luckyaf.wifly.utils
import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.support.v4.content.FileProvider
import android.util.Log
import com.luckyaf.kommon.Kommon

import java.io.File

/**
 * 类描述：
 * @author Created by luckyAF on 2019-03-12
 *
 */
object UriUtils {
    /**
     * File to uri.
     *
     * @param file The file.
     * @return uri
     */
    fun file2Uri(@NonNull file: File): Uri {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val authority = Kommon.context.packageName + ".fileprovider"
            return FileProvider.getUriForFile(Kommon.context, authority, file)
        } else {
            return Uri.fromFile(file)
        }
    }

    /**
     * Uri to file.
     *
     * @param uri The uri.
     * @return file
     */
    fun uri2File(@NonNull uri: Uri): File? {
        val authority = uri.authority
        val scheme = uri.scheme
        if (ContentResolver.SCHEME_FILE == scheme) {
            val path = uri.path
            if (path != null) return File(path)
            return null
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
                Kommon.context,
                uri
            )
        ) {
            if ("com.android.externalstorage.documents" == authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return File("${Environment.getExternalStorageDirectory()}/ + ${split[1]}")
                }
                Log.d("UriUtils", "$uri parse failed. -> 1")
                return null
            } else if ("com.android.providers.downloads.documents" == authority) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getFileFromUri(contentUri, 2)
            } else if ("com.android.providers.media.documents" == authority) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                val contentUri: Uri
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                } else {
                    Log.d("UriUtils", uri.toString() + " parse failed. -> 3")
                    return null
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getFileFromUri(contentUri, selection, selectionArgs, 4)
            } else if (ContentResolver.SCHEME_CONTENT == scheme) {
                return getFileFromUri(uri, 5)
            } else {
                Log.d("UriUtils", uri.toString() + " parse failed. -> 6")
                return null
            }
        } else if (ContentResolver.SCHEME_CONTENT == scheme) {
            return getFileFromUri(uri, 7)
        } else {
            Log.d("UriUtils", uri.toString() + " parse failed. -> 8")
            return null
        }
    }

    private fun getFileFromUri(uri: Uri, code: Int): File? {
        return getFileFromUri(uri, null, null, code)
    }

    private fun getFileFromUri(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        code: Int
    ): File? {
        val cursor = Kommon.context.contentResolver.query(
            uri, arrayOf("_data"), selection, selectionArgs, null
        )
        if (cursor == null) {
            Log.d("UriUtils", "$uri parse failed(cursor is null). -> $code" )
            return null
        }
        try {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex("_data")
                if (columnIndex > -1) {
                    return File(cursor.getString(columnIndex))
                } else {
                    Log.d(
                        "UriUtils",
                        "$uri parse failed(columnIndex: $columnIndex is wrong). -> $code"
                    )
                    return null
                }
            } else {
                Log.d(
                    "UriUtils",
                     "$uri parse failed(moveToFirst return false). -> $code"
                )
                return null
            }
        } catch (e: Exception) {
            Log.d("UriUtils", "$uri parse failed. -> $code")
            return null
        } finally {
            cursor.close()
        }
    }
}