package com.eva.goldenhorses.network

import com.eva.goldenhorses.model.JugadorRanking
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FirebaseApi {
    @GET("ranking/{fecha}.json")
    suspend fun getRankingPorFecha(@Path("fecha") fecha: String, @Query("auth") token: String): Map<String, JugadorRanking>
}


