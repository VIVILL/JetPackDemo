package com.example.jetpackdemo.viewmodel

import android.util.Log
import android.view.MotionEvent
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.*
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "WanAndroidViewModel"

@HiltViewModel
class WanAndroidViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel(){
    private val _bannerListStateFlow = MutableStateFlow<List<Banner>>(listOf()/*ArrayList()*/)

    val bannerListStateFlow: StateFlow<List<Banner>> = _bannerListStateFlow

    fun updateBannerList() {
        // 更新  value 数据
        viewModelScope.launch(exceptionHandler) {
            _bannerListStateFlow.value = repository.getBannerFlow()
                .catch { throwable ->
                    // Catch exceptions in all down stream flow
                    // Any error occurs after this catch operator
                    // will not be caught here
                    println(throwable)
                }
                .stateIn(viewModelScope)
                .value
        }
    }

    // 获取文章
    fun getArticle(): Flow<PagingData<Article>> {
        return repository.getHomePageArticle().cachedIn(viewModelScope)
    }

    /**
     * 请求每日一问数据
     */
    fun getDailyQuestion(): Flow<PagingData<Article>> {
        return repository.getDailyQuestion().cachedIn(viewModelScope)
    }

    /**
     * 请求广场数据
     */
    fun getSquareData(): Flow<PagingData<Article>> {
        return repository.getSquareData().cachedIn(viewModelScope)
    }

    // 使用 SharedFlow
    private val _collectAction = MutableSharedFlow<CollectAction>()
    val collectAction: SharedFlow<CollectAction> = _collectAction

    fun collect(id: Int,position: Int) {
        viewModelScope.launch(exceptionHandler) {
            try{
                val response: WanAndroidResponse<Article> = repository.collect(id)
                Log.d(TAG,"response.errorCode = ${response.errorCode}")
                if(response.errorCode == 0) {
                    /*
                    *  "data": null,
                       "errorCode": 0,
                       "errorMsg": ""
                    * */
                    _collectAction.emit(CollectAction.Success("收藏成功",position))
                } else {
                    _collectAction.emit(CollectAction.Error("收藏失败",position))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }


    private val _unCollectAction = MutableSharedFlow<UnCollectAction>()
    val unCollectAction: SharedFlow<UnCollectAction> = _unCollectAction

    fun unCollect(id: Int,position: Int) {
        viewModelScope.launch(exceptionHandler) {
            try{
                val response: WanAndroidResponse<Article> = repository.unCollect(id)
                Log.d(TAG,"inner unCollect response.errorCode = ${response.errorCode}")
                if(response.errorCode == 0) {
                    /*
                    *  "data": null,
                       "errorCode": 0,
                       "errorMsg": ""
                    * */
                    _unCollectAction.emit(UnCollectAction.Success("取消收藏成功",position))
                } else {
                    _unCollectAction.emit(UnCollectAction.Error("取消收藏失败",position))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    // 获取收藏数据
    fun getCollection(): Flow<PagingData<Collect>> {
        return repository.getCollection().cachedIn(viewModelScope)
    }

    private val _unCollectByCollectionAction = MutableSharedFlow<UnCollectAction>()
    val unCollectByCollectionAction: SharedFlow<UnCollectAction> = _unCollectByCollectionAction

    fun unCollectByCollection(id: Int,originId: Int,position: Int) {
        viewModelScope.launch(exceptionHandler) {
            try{
                val response: WanAndroidResponse<Collect> = repository.unCollectByCollection(id,originId)
                Log.d(TAG,"inner unCollectByCollection response.errorCode = ${response.errorCode}")
                if(response.errorCode == 0) {
                    _unCollectByCollectionAction.emit(UnCollectAction.Success("取消收藏成功",position))
                } else {
                    _unCollectByCollectionAction.emit(UnCollectAction.Error("取消收藏失败",position))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }


    private val _autoScrollAction = MutableSharedFlow<AutoScrollAction>()
    val autoScrollAction: SharedFlow<AutoScrollAction> = _autoScrollAction

    private var isAutoScroll: Boolean = true

    private lateinit var autoScrollJob: Job


    fun autoScroll() {
        Log.d(TAG,"inner autoScroll isAutoScroll = $isAutoScroll")
        if (this::autoScrollJob.isInitialized) {
            autoScrollJob.cancel()
            Log.d(TAG, "after autoScrollJob.cancel()")
        }
        isAutoScroll = true
        autoScrollJob = viewModelScope.launch(exceptionHandler) {
            repeat(Int.MAX_VALUE){
                Log.d(TAG,"inner repeat")
                delay(3000L)
                Log.d(TAG,"after delay 3000L isAutoScroll = $isAutoScroll")
                if (isAutoScroll){
                    _autoScrollAction.emit(AutoScrollAction.AutoScroll)
                }
            }
        }
    }

    fun cancelAutoScroll() {
        Log.d(TAG,"inner cancelAutoScroll")
        isAutoScroll = false
        if (this::autoScrollJob.isInitialized) {
            autoScrollJob.cancel()
            Log.d(TAG, "after autoScrollJob.cancel()")
        }
    }

    private lateinit var pauseAutoScrollJob: Job

    fun touchViewPager2(event: MotionEvent) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "inner MotionEvent.ACTION_DOWN")
                    isAutoScroll = false
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL-> {
                    Log.d(TAG, "inner MotionEvent.ACTION_UP or ACTION_CANCEL")
                    // 先取消之前的任务
                    if (::pauseAutoScrollJob.isInitialized) {
                        pauseAutoScrollJob.cancel()
                        Log.d(TAG, "after job.cancel()")
                    }
                    Log.d(TAG, "after set autoScroll = false")
                    pauseAutoScrollJob = viewModelScope.launch(exceptionHandler){
                            Log.d(TAG, "after launch")
                            // 等待5s后 重新开始 无限循环
                            delay(5000L)
                            isAutoScroll = true
                            Log.d(TAG, "after delay 5000L, after set autoScroll = true")
                    }
                }
                else -> {}
        }
    }

    private val _recyclerviewTouchAction = MutableSharedFlow<TouchAction>()
    val recyclerviewTouchAction: SharedFlow<TouchAction> = _recyclerviewTouchAction

    fun touchRecyclerview(event: MotionEvent) {
        viewModelScope.launch(exceptionHandler) {
            _recyclerviewTouchAction.emit(TouchAction.Touch(event))
        }
    }
}

sealed class AutoScrollAction {
    object AutoScroll: AutoScrollAction()
}

sealed class TouchAction {
    data class Touch(val event: MotionEvent): TouchAction()
}

sealed class CollectAction {
    data class Success(val message: String,val position: Int): CollectAction()
    data class Error(val message: String,val position: Int): CollectAction()
}

sealed class UnCollectAction {
    data class Success(val message: String,val position: Int): UnCollectAction()
    data class Error(val message: String,val position: Int): UnCollectAction()
}

