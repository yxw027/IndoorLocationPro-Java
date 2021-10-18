package com.hust.mvvm.http

import android.util.Log
import com.hust.mvvm.config.AppConfig
import com.hust.mvvm.http.constant.HttpConstant
import com.hust.mvvm.http.interceptor.CacheInterceptor
import com.hust.mvvm.http.interceptor.CookieInterceptor
import com.hust.mvvm.http.interceptor.HeaderInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * @author admin
 * @date 2018/11/21
 * @desc RetrofitFactory
 * 创建API接口实例需要实现 两个方法：baseUrl()  attachApiService()
 * 调用时需要只需要.service
 */
abstract class BaseApiService<T> {

    private var apiService: T
    private var mBaseUrl = ""
    private var retrofit: Retrofit? = null

    abstract fun baseUrl(): String

    abstract fun attachApiService(): Class<T>

    init {
        mBaseUrl = this.baseUrl()
        if (mBaseUrl.isEmpty()) {
            throw RuntimeException("base url can not be empty!")
        }
        apiService = getRetrofit()!!.create(this.attachApiService())
    }

    fun getApiService():T{
        return apiService
    }
    /**
     * 获取 Retrofit 实例对象
     */
    private fun getRetrofit(): Retrofit? {
        if (retrofit == null) {
            synchronized(BaseApiService::class.java) {
                if (retrofit == null) {
                    retrofit = Retrofit.Builder()
                        .baseUrl(mBaseUrl)  // baseUrl
                        .client(attachOkHttpClient())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                }
            }
        }
        return retrofit
    }

    /**
     * 获取 OkHttpClient 实例对象
     * 子类可重写，自定义 OkHttpClient
     */
    open fun attachOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
        val httpLoggingInterceptor = HttpLoggingInterceptor {
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
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        } else {
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.NONE
        }

        //设置 请求的缓存的大小跟位置
        val cacheFile = File(AppConfig.getApplication().cacheDir, "cache")
        val cache = Cache(cacheFile, HttpConstant.MAX_CACHE_SIZE)

        builder.run {
            //网页请求日志输出
            addInterceptor(httpLoggingInterceptor)
            //请求报文添加Cookie
            addInterceptor(HeaderInterceptor())
            //响应报文保存cookie
            addInterceptor(CookieInterceptor())
            addInterceptor(CacheInterceptor())
            cache(cache)  //添加缓存
            //设置请求超时时间
            callTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            connectTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(HttpConstant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true) // 错误重连
            // cookieJar(CookieManager())
        }
        return builder.build()
    }

}