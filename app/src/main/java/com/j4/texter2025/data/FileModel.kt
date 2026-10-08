package com.j4.texter2025.data

import java.util.UUID

data class FileModel(
    val name: String,
    val content: String,
    val memo: String = "", // Preview/memo text
    val id: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val keepBothFiles: Boolean = false,
    val memoVisible: Boolean = false // Track if memo area should be visible for this file
)
