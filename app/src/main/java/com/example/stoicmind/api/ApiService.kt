package com.example.stoicmind.api

import com.example.stoicmind.models.QuoteResponse
import retrofit2.http.GET

interface ApiService {

    @GET("quote")
    suspend fun getRandomQuote(): QuoteResponse
}