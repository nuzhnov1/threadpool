package com.nuzhnov.threadpool.app.network

import com.nuzhnov.threadpool.app.network.models.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory

val responseFactory: PolymorphicJsonAdapterFactory<Response> = PolymorphicJsonAdapterFactory.of(
    Response::class.java, "status"
)
    .withSubtype(Response.Success::class.java, "success")
    .withSubtype(Response.Failed::class.java, "failed")
    .withDefaultValue(Response.Unknown)

val moshi: Moshi = Moshi.Builder()
    .add(responseFactory)
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl("https://api.getgeoapi.com")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

val currencyService: CurrencyService = retrofit.create(CurrencyService::class.java)
