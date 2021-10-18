package com.hust.mvvm.http

import android.util.Log
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.hust.mvvm.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 创建API接口实例需要调用 getService(API,baseUrl) 方法
 * Created by shisenkun on 2019-06-18.
 */
abstract class BaseRetrofitClient {

    companion object {
        private const val TIME_OUT = 5
    }

    private val client: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()

            //新建log拦截器
            val loggingInterceptor = HttpLoggingInterceptor {
                var log = it
                // 因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
                // 把4*1024的MAX字节打印长度改为2001字符数
                val maxLength = 2001
                // 大于4000时
                while (log.length > maxLength) {
                    Log.d("http---", log.substring(0, maxLength))
                    log = log.substring(maxLength)
                }
                // 剩余部分
                Log.d("http--->", log)
            }
            if (AppConfig.isDebug) {
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            } else {
                loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            }

            builder.addInterceptor(loggingInterceptor)
                .connectTimeout(TIME_OUT.toLong(), TimeUnit.SECONDS)

            handleBuilder(builder)

            return builder.build()
        }

    protected abstract fun handleBuilder(builder: OkHttpClient.Builder)

    fun <T> getService(serviceClass: Class<T>, baseUrl: String): T {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
            .baseUrl(baseUrl)
            .build().create(serviceClass)
    }

}
