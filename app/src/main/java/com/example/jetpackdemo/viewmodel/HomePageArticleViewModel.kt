package com.example.jetpackdemo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.Banner
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomePageArticleViewModel"
@HiltViewModel
class HomePageArticleViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel() {
    private val _banner = MutableStateFlow<List<Banner>>(listOf())
    val banner: StateFlow<List<Banner>> = _banner

    init {
        viewModelScope.launch(exceptionHandler) {
            _banner.value = repository.getBannerFlow()
                .catch { throwable ->
                    println(throwable)
                }
                .stateIn(viewModelScope)
                .value
        }
    }

    fun loadBanner(){
        Log.d(TAG,"inner loadBanner")
        // 更新  value 数据
        viewModelScope.launch(exceptionHandler) {
            _banner.value = repository.getBannerFlow()
                .catch { throwable ->
                    println(throwable)
                }
                .stateIn(viewModelScope)
                .value
        }
    }

    // 首页文章
    val homePageArticleFlow = repository.getHomePageArticle().cachedIn(viewModelScope)

    // 每日一问数据
    val dailyQuestionFlow = repository.getDailyQuestion().cachedIn(viewModelScope)

    // 广场数据
    val squareDataFlow = repository.getSquareData().cachedIn(viewModelScope)

}