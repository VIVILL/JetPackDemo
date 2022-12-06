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

private const val TAG = "HomePageViewModel"
@HiltViewModel
class HomePageViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel() {
    private val _banner = MutableStateFlow<List<Banner>>(listOf())
    val banner: StateFlow<List<Banner>> = _banner

    init {
        loadBanner()
    }

    fun loadBanner(){
        Log.d(TAG,"inner loadBanner")
        // 更新  value 数据
        viewModelScope.launch(exceptionHandler) {
            val list = ArrayList<Banner>()
            val value = repository.getBannerFlow()
                .catch { throwable ->
                    println(throwable)
                }
                .stateIn(viewModelScope)
                .value
            // 将获取的数据 进行拼接处理
            if (value.isNotEmpty()){
                list.add(value[value.size-1])
                list.addAll(value)
                list.add(value[0])
                Log.d(TAG," list size = ${list.size} list = $list")
                _banner.value = list
            }
        }
    }

    // 初始化首页文章数据
    val homePageArticleFlow = repository.getHomePageArticle().cachedIn(viewModelScope)

    // 初始化每日一问数据
    val dailyQuestionFlow = repository.getDailyQuestion().cachedIn(viewModelScope)

    // 初始化广场数据
    val squareDataFlow = repository.getSquareData().cachedIn(viewModelScope)

}