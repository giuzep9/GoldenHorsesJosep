package com.eva.goldenhorses.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


object ApiClient {
    private const val BASE_URL = "https://golden-horses-22f82-default-rtdb.firebaseio.com/" // Asegúrate de cambiar esto

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val rankingApi: RankingApi = retrofit.create(RankingApi::class.java)
}

/*object ApiClient {
    private const val BASE_URL = "https://golden-horses-22f82-default-rtdb.firebaseio.com/" // Cambia a tu URL

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val rankingApi: RankingApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RankingApi::class.java)
    }
}
*/