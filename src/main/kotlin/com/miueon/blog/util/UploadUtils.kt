package com.miueon.blog.util

import java.io.File

class UploadUtils {
    companion object{
        val md_path_prefix = "static/md"
        fun getMdDirFile(): File {
            val fileDirPath = "src/main/resources/${md_path_prefix}"
            val fileDir = File(fileDirPath)
            if (!fileDir.exists()) {
                fileDir.mkdirs()
            }

            return fileDir
        }
    }
}