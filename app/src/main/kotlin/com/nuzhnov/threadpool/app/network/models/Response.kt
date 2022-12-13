package com.nuzhnov.threadpool.app.network.models

import com.squareup.moshi.JsonClass

sealed interface Response {
    val status: Status

    @JsonClass(generateAdapter = true)
    data class Success(
        val currencies: Map<String, String>
    ) : Response {

        override val status: Status = Status.SUCCESS
    }

    @JsonClass(generateAdapter = true)
    data class Failed(
        val error: Error
    ) : Response {

        override val status: Status = Status.FAILED
    }

    object Unknown : Response {

        override val status: Status = Status.UNKNOWN
    }
}
