package com.nuzhnov.threadpool.app.network.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Error(
    val message: String,
    val code: Int
)
