package com.eva.goldenhorses.network

import com.eva.goldenhorses.model.JugadorRanking
import retrofit2.http.GET
import retrofit2.http.Path

interface FirebaseApi {
    @GET("{fecha}.json")
    suspend fun getRankingPorFecha(@Path("fecha") fecha: String): Map<String, JugadorRanking>
}

