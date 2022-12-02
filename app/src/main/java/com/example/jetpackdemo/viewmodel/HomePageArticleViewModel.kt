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

@HiltViewModel
class HomePageArticleViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel() {
    // 首页文章
    val homePageArticleFlow: Flow<PagingData<Article>> = repository.getHomePageArticle().cachedIn(viewModelScope)

}