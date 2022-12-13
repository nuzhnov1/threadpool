package com.nuzhnov.threadpool.app.network

import com.nuzhnov.threadpool.app.network.models.Response
import retrofit2.http.GET
import retrofit2.Call

interface CurrencyService {
    @GET("/v2/currency/list?api_key=${API_KEY}&format=json")
    fun getCurrencies(): Call<Response>


    private companion object {
        const val API_KEY = "956e47fba83bb83bbfe7facdb468d3ddc1300c37"
    }
}
