package com.example.jetpackdemo.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.jetpackdemo.api.WanAndroidApi
import com.example.jetpackdemo.bean.Article

private const val DailyQuestion_STARTING_PAGE_INDEX = 1

private const val TAG = "DailyQuestionPagingSource"
class DailyQuestionPagingSource(private val wanAndroidApi: WanAndroidApi):
    PagingSource<Int, Article>() {

    override fun getRefreshKey(state: PagingState<Int, Article>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: DailyQuestion_STARTING_PAGE_INDEX
        Log.d(TAG,"page = $page")
        return try {
            val response = wanAndroidApi.getDailyQuestion(page)
            // 获取 List<DailyQuestion>
            val dailyQuestionList = response.data.datas
            LoadResult.Page(
                data = dailyQuestionList,
                prevKey = if (page == DailyQuestion_STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (page == response.data.pageCount) null else page + 1,
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}