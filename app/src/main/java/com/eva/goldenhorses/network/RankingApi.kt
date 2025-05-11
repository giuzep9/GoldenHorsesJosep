package com.eva.goldenhorses.network

/*import com.eva.goldenhorses.model.JugadorRanking
import retrofit2.http.GET

interface RankingApi {
    @GET("ranking.json") // Ajusta esta ruta a tu endpoint real
    suspend fun obtenerRanking(): List<JugadorRanking>
}*/

import com.eva.goldenhorses.model.JugadorRanking
import retrofit2.http.GET
import retrofit2.http.Query

interface RankingApi {
    @GET("ranking.json")
    suspend fun obtenerRanking(@Query("auth") token: String): Map<String, JugadorRanking>
}
