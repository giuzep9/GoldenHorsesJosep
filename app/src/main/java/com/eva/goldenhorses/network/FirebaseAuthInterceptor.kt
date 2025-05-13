package com.eva.goldenhorses.network

import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response

class FirebaseAuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val user = FirebaseAuth.getInstance().currentUser
        val tokenResult = user?.getIdToken(false)?.result
        val token = tokenResult?.token

        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        val newUrl = originalRequest.url().newBuilder()
            .addQueryParameter("auth", token)
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}

