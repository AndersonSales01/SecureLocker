package com.example.securelocker.data

import java.io.File

data class FileEntity(
    val fileName: String,
    val file: File,
    val fileSize: String
)