package com.example.jetpackdemo.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.jetpackdemo.api.WanAndroidApi
import com.example.jetpackdemo.bean.Collect

private const val ARTICLE_STARTING_PAGE_INDEX = 0
private const val TAG = "CollectionPagingSource"

class CollectionPagingSource(private val wanAndroidApi: WanAndroidApi): PagingSource<Int, Collect>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Collect> {
        val page = params.key ?: ARTICLE_STARTING_PAGE_INDEX
        Log.d(TAG,"page = $page")
        return try {
            val response = wanAndroidApi.getCollection(page)
            // 获取 List<Collect>
            val collectList = response.data.datas
            LoadResult.Page(
                data = collectList,
                prevKey = if (page == ARTICLE_STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (page == response.data.pageCount) null else page + 1,
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Collect>): Int? {
        return null
    }
}