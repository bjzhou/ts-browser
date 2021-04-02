package com.hinnka.tsbrowser.ext

import android.webkit.MimeTypeMap
import java.io.File

val File.mimeType: String?
    get() {
        val ext = MimeTypeMap.getFileExtensionFromUrl(absolutePath)
        return ext?.let {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(it)
        }
    }