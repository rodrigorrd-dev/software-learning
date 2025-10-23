package com.academic.softwarelearning.domain.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File

class ArquivoStorageManager(private val context: Context) {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun salvarArquivo(bytes: ByteArray, nomeArquivo: String, mimeType: String): Uri? {
        val resolver = context.contentResolver
        val uri: Uri?

        val mediaStoreUri = when {
            mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    when {
                        mimeType.startsWith("image/") -> "Pictures/SoftwareLearning"
                        mimeType.startsWith("video/") -> "Movies/SoftwareLearning"
                        mimeType.startsWith("audio/") -> "Music/SoftwareLearning"
                        else -> "Download/SoftwareLearning"
                    }
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            uri = resolver.insert(mediaStoreUri, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(bytes)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
        } else {
            // Android < Q
            val dir = when {
                mimeType.startsWith("image/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                mimeType.startsWith("video/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                mimeType.startsWith("audio/") -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                else -> Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, nomeArquivo)
            file.outputStream().use { it.write(bytes) }
            uri = Uri.fromFile(file)
        }

        return uri
    }
}
