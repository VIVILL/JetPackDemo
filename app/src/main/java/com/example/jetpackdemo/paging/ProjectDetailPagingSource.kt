package com.example.jetpackdemo.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.jetpackdemo.api.WanAndroidApi
import com.example.jetpackdemo.bean.ProjectContent

private const val ProjectDetail_STARTING_PAGE_INDEX = 1

private const val TAG = "ProjectDetailPagingSource"
class ProjectDetailPagingSource(private val wanAndroidApi: WanAndroidApi,private val id: Int):
    PagingSource<Int, ProjectContent>() {

    override fun getRefreshKey(state: PagingState<Int, ProjectContent>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProjectContent> {
        val page = params.key ?: ProjectDetail_STARTING_PAGE_INDEX
        Log.d(TAG,"page = $page id = $id")
        return try {
            val response = wanAndroidApi.loadContentById(page,id)
            // 获取 List<ProjectContent>
            val projectContentList = response.data.datas
            LoadResult.Page(
                data = projectContentList,
                prevKey = if (page == ProjectDetail_STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (page == response.data.pageCount) null else page + 1,
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }
}