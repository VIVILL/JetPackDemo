package com.example.jetpackdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.Article
import com.example.jetpackdemo.repository.WanAndroidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

private const val TAG = "DailyQuestionViewModel"

@HiltViewModel
class DailyQuestionViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel() {

    // 每日一问数据
    val dailyQuestionFlow: Flow<PagingData<Article>> = repository.getDailyQuestion().cachedIn(viewModelScope)


}