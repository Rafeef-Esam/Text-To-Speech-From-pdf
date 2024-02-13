package com.example.texttospeech.utilites

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.example.texttospeech.data.Attachment

object FileUtils {

    fun uploadFile(resultUri: Uri, context: Context): Attachment {
        val fileName = fileName(context, resultUri)
        val name = fileName.substring(0,fileName.lastIndexOf("."))

        return Attachment(attachmentUri = resultUri, attachmentFileName = name)
    }

    private fun fileName(context: Context, uri: Uri): String {
        val filename: String
        val cursor = context.contentResolver?.query(uri,null,null,null,null)
        if(cursor == null) {
            filename = uri.path ?: ""
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME)
            filename = cursor.getString(idx)
            cursor.close()
        }

        val name = filename.substring(0,filename.lastIndexOf("."))
        val extension = filename.substring(filename.lastIndexOf(".")+1)
        return "$name.$extension"
    }
}