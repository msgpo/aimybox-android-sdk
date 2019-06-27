package com.justai.aimybox.api.aimybox

import android.os.Build
import androidx.annotation.RequiresApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class RetrofitHttpWorker(apiUrl: String) : BaseHttpWorker() {
    private val retrofit = Retrofit.Builder()
        .client(createHttpClient())
        .baseUrl(apiUrl)
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(GsonConverterFactory.create(gsonInstance))
        .build()

    private val api = retrofit.create(AimyboxRetrofitApi::class.java)

    override suspend fun requestAsync(request: AimyboxRequest) = api.performRequestAsync(request).await()

    private fun createHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()

        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        return builder.build()
    }
}