package com.hust.mvvm.base

import com.hust.mvvm.http.constant.HttpErrorCode
import com.hust.mvvm.http.exception.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @author admin
 * @date 2019/11/1
 * @desc
 */
abstract class BaseRepository {

//    object ApiWaz: BaseApiService<Api>() {
//        override fun baseUrl(): String {
//            return Api.BASE_URL
//        }
//
//        override fun attachApiService(): Class<Api> {
//            return Api::class.java
//        }
//    }
//
//    protected fun getApiService(): Api {
//        return ApiWaz.getApiService()
//    }


    suspend fun <T> apiCall(call: suspend () -> BaseResponse<T>): T? {
        return withContext(Dispatchers.IO) {
            val response = call.invoke()
            executeResponse(response)
        }
    }

    private fun <T> executeResponse(response: BaseResponse<T>): T? {
        when (response.errorCode) {
            HttpErrorCode.SUCCESS -> {
                return response.data
            }
            else -> {
                throw ApiException(response.errorCode, response.errorMsg)
            }
        }
    }

}
