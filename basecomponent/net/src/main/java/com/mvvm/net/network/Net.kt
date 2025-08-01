package com.mvvm.net.network

import com.mvvm.net.BuildConfig
import okhttp3.Interceptor
import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Net {
    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null
    private var timeOut = 20000L
    fun getRetrofit(baseUrl: String, time: Long = 20000L): Retrofit {
        timeOut = time
        if (null == retrofit) {
            if (null == okHttpClient) {
                okHttpClient = getOkHttpClient()
            }
            //Retrofit2后使用build设计模式
            retrofit = Retrofit.Builder()
                //设置服务器路径
                .baseUrl("$baseUrl/")
                //添加转化库，默认是Gson  DecodeConverterFactory DecodeConverterFactory
                //                    .addConverterFactory(DecodeConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                //设置使用okhttp网络请求
                .client(okHttpClient!!)
                .build()
            return retrofit!!
        }
        return retrofit!!
    }

    private fun getOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor()
        val logging = HttpLoggingInterceptor()

        logging.level = HttpLoggingInterceptor.Level.BODY
        if (BuildConfig.DEBUG) {
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        }
        val headerInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
            //请求头携token
            builder.addHeader("Authorization", "")
            chain.proceed(builder.build())
        }
        val okhttpBuilder = OkHttpClient.Builder()
            .connectTimeout(timeOut, TimeUnit.SECONDS)
            .addInterceptor(headerInterceptor)
            .addInterceptor(HttpLogInterceptor())
            .writeTimeout(timeOut, TimeUnit.SECONDS)
            .readTimeout(timeOut, TimeUnit.SECONDS)
            .cookieJar(MyCookiesJar())
        if (BuildConfig.DEBUG) {
            okhttpBuilder.sslSocketFactory(
                SSLSocketClient.sSLSocketFactory,
                SSLSocketClient.trustManager[0]
            )
        }
        return okhttpBuilder.build()
    }
}
