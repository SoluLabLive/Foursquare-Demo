package com.example.firebaseandfoursquareapidemo.network

import com.example.firebaseandfoursquareapidemo.Explore.Explore
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface FourSquareService {

    @GET("venues/explore/")
    fun requestExplore(
        @Query("client_id") client_id: String,
        @Query("client_secret") client_secret: String,
        @Query("v") v: String,
        @Query("ll") ll: String,
        @Query("query") query: String,
        @Query("radius") radius: String

    ): Call<Explore>

    companion object {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.foursquare.com/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}