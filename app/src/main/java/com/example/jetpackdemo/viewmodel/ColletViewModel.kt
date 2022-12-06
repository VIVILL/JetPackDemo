package com.example.jetpackdemo.viewmodel

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.*
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "CollectViewModel"

@HiltViewModel
class CollectViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel(){
    // 使用 SharedFlow
    private val _collectAction = MutableSharedFlow<CollectAction>()
    val collectAction: SharedFlow<CollectAction> = _collectAction

    fun collect(id: Int) {
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
                    _collectAction.emit(CollectAction.Success("收藏成功"))
                } else {
                    _collectAction.emit(CollectAction.Error("收藏失败"))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }


    private val _unCollectAction = MutableSharedFlow<UnCollectAction>()
    val unCollectAction: SharedFlow<UnCollectAction> = _unCollectAction

    fun unCollect(id: Int) {
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
                    _unCollectAction.emit(UnCollectAction.Success("取消收藏成功"))
                } else {
                    _unCollectAction.emit(UnCollectAction.Error("取消收藏失败"))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }

    // 获取收藏数据
/*    fun getCollection(): Flow<PagingData<Collect>> {
        return repository.getCollection().cachedIn(viewModelScope)
    }*/
    // 初始化 收藏 数据
    val collectionFlow = repository.getCollection().cachedIn(viewModelScope)

    private val _unCollectByCollectionAction = MutableSharedFlow<UnCollectAction>()
    val unCollectByCollectionAction: SharedFlow<UnCollectAction> = _unCollectByCollectionAction

    fun unCollectByCollection(id: Int,originId: Int) {
        viewModelScope.launch(exceptionHandler) {
            try{
                val response: WanAndroidResponse<Collect> = repository.unCollectByCollection(id,originId)
                Log.d(TAG,"inner unCollectByCollection response.errorCode = ${response.errorCode}")
                if(response.errorCode == 0) {
                    _unCollectByCollectionAction.emit(UnCollectAction.Success("取消收藏成功"))
                } else {
                    _unCollectByCollectionAction.emit(UnCollectAction.Error("取消收藏失败"))
                }
            }catch (e: Exception){
                e.printStackTrace()
            }

        }
    }


}


sealed class CollectAction {
    data class Success(val message: String): CollectAction()
    data class Error(val message: String): CollectAction()
}

sealed class UnCollectAction {
    data class Success(val message: String): UnCollectAction()
    data class Error(val message: String): UnCollectAction()
}

