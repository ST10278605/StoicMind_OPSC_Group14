package com.example.stoicmind.api

import com.example.stoicmind.models.Quote
import retrofit2.http.GET

interface ApiService {

    @GET("quote")
    suspend fun getRandomQuote(): Quote
}