package com.hust.mvvm.base

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.hust.mvvm.utils.LogUtil
import com.hust.mvvm.http.exception.ApiException
import com.yechaoa.yutilskt.ToastUtil
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * @author chenxz
 * @date 2019/11/1
 * @desc BaseViewModel
 */
open class BaseViewModel : ViewModel(), LifecycleObserver {

    private val mViewModelName by lazy { javaClass.simpleName }

    /**********************************/

    val needLogin = MutableLiveData<Boolean>().apply { value = false }

    /**
     * 创建并执行协程 Coroutines
     * @param block 协程中执行
     * @param error 错误时执行
     * @param cancel 取消时执行
     * @param showErrorToast 是否弹出错误吐司
     * @return Job API 文档 https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/index.html
     *
     * CoroutineScope.launch 函数返回的是一个 Job 对象，代表一个异步的任务。
     * viewModelScope 也是继承 CoroutineScope的
     * Job 具有生命周期并且可以取消。
     * Job 还可以有层级关系，一个Job可以包含多个子Job，当父Job被取消后，所有的子Job也会被自动取消；
     * 当子Job被取消或者出现异常后父Job也会被取消。
     */
    protected fun launch(
        block: suspend CoroutineScope.() -> Unit,
        error: (suspend (Exception) -> Unit)? =null,
        cancel: (suspend (Exception) -> Unit)? =null,
        showErrorToast: Boolean = true
    ): Job {
        return viewModelScope.launch {
            try {
                //apiCall,返回BaseResponse
                block.invoke(this)
            } catch (e: Exception) {
                //处理错误
                when (e) {
                    is CancellationException -> {
                        cancel?.invoke(e)
                    }
                    else -> {
                        onError(e, showErrorToast)
                        error?.invoke(e)
                    }
                }
            }
        }
    }
    /**
     * 运行在主线程，主要进行数据库操作
     * */
    protected fun launchMain(
        block: suspend CoroutineScope.() -> Unit,
        error: (suspend (Exception) -> Unit)? =null,
        cancel: (suspend (Exception) -> Unit)? =null,
        showErrorToast: Boolean = true
    ): Job {
        return MainScope().launch(Dispatchers.IO) {
            try {
                //apiCall,返回BaseResponse
                block.invoke(this)
            } catch (e: Exception) {
                //处理错误
                when (e) {
                    is CancellationException -> {
                        cancel?.invoke(e)
                    }
                    else -> {
                        onError(e, showErrorToast)
                        error?.invoke(e)
                    }
                }
            }
        }
    }

    /**
     * 创建并执行协程
     * @param block 协程中执行
     * @return Deferred<T> 继承自 Job 额外多3个方法
     */
    protected fun <T> async(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return viewModelScope.async { block.invoke(this) }
    }

    /**
     * 取消协程 会抛出CancellationException
     * @param job 协程job
     */
    protected fun cancelJob(job: Job?) {
        if (job != null && job.isActive && !job.isCompleted && !job.isCancelled) {
            job.cancel()
        }
    }

    /**
     * 统一处理错误
     * @param e 异常
     * @param showErrorToast 是否显示错误吐司
     */
    private fun onError(e: Exception, showErrorToast: Boolean) {
        when (e) {
            is ApiException -> {
                when (e.errorCode) {
                    -1001 -> {
                        if (showErrorToast) ToastUtil.show(e.errorMsg)
                        needLogin.value = true
                    }
                    // 其他错误
                    else -> {
                        if (showErrorToast) ToastUtil.show(e.errorMsg)
                    }
                }
                LogUtil.e(e.errorMsg)
            }
            // 网络请求失败
            is ConnectException, is SocketTimeoutException, is UnknownHostException, is retrofit2.HttpException -> {
                if (showErrorToast) ToastUtil.show(" 网络请求失败")
                LogUtil.e(mViewModelName+" 网络请求失败" + e.message)
            }
            // 数据解析错误
            is JsonParseException -> {
                if (showErrorToast) ToastUtil.show(" 数据解析错误")
                LogUtil.e(mViewModelName+" 数据解析错误" + e.message)
            }
            // 其他错误
            else -> {
                if (showErrorToast) ToastUtil.show(e.message ?: return)
                LogUtil.e(e.message ?: return)
            }

        }
    }
    /**
     * json提交 转RequestBody （表单提交 @FieldMap）
     */
    protected fun toRequestBody(params: Any?): RequestBody {
        return Gson().toJson(params).toRequestBody("application/json".toMediaTypeOrNull())
    }
    /**********************************/
    /*
    private val mException: MutableLiveData<Exception> = MutableLiveData()

    val showLoading = MutableLiveData<Boolean>()

    fun scopeLaunch(
        block: suspend CoroutineScope.() -> Unit,
        onException: ((Throwable) -> Unit)? = null
    ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            LogUtil.e("ApiException", throwable.message ?: "scopeLaunch")
            showLoading.postValue(false)
            when (throwable) {
                is ApiException -> {
                    ToastUtil.show(throwable.errorMsg)
                }
            }
            onException?.invoke(throwable)
        }
        viewModelScope.launch(handler) {
            block()
        }
    }

    private fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        MainScope().launch { block() }
    }

    suspend fun <T> launchIO(block: suspend CoroutineScope.() -> T) {
        withContext(Dispatchers.IO) {
            block
        }
    }

    fun launch(tryBlock: suspend CoroutineScope.() -> Unit) {
        launchOnUI {
            tryCatch(tryBlock, {}, {}, true)
        }
    }

    fun launchOnUITryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean
    ) {
        launchOnUI {
            tryCatch(tryBlock, catchBlock, finallyBlock, handleCancellationExceptionManually)
        }
    }

    fun launchOnUITryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ) {
        launchOnUI {
            tryCatch(tryBlock, {}, {}, handleCancellationExceptionManually)
        }
    }

    private suspend fun tryCatch(
        tryBlock: suspend CoroutineScope.() -> Unit,
        catchBlock: suspend CoroutineScope.(Throwable) -> Unit,
        finallyBlock: suspend CoroutineScope.() -> Unit,
        handleCancellationExceptionManually: Boolean = false
    ) {
        coroutineScope {
            try {
                tryBlock()
            } catch (e: Exception) {
                if (e !is CancellationException || handleCancellationExceptionManually) {
                    mException.value = e
                    catchBlock(e)
                } else {
                    throw e
                }
            } finally {
                finallyBlock()
            }
        }
    }

    */
}
