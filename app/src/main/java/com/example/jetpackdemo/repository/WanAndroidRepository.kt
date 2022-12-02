package com.example.jetpackdemo.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.jetpackdemo.api.WanAndroidApi
import com.example.jetpackdemo.bean.*
import com.example.jetpackdemo.paging.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WanAndroidRepository @Inject constructor(
    private val wanAndroidApi: WanAndroidApi
){
    companion object {
        private const val HOME_ARTICLE_PAGE_SIZE = 20
        private const val PROJECT_CONTENT_PAGE_SIZE = 10

    }

    fun getHomePageArticle(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false,
                pageSize = HOME_ARTICLE_PAGE_SIZE
            ),
            pagingSourceFactory = { HomeArticlePagingSource(wanAndroidApi) }
        ).flow
    }

    fun getBannerFlow(): Flow<List<Banner>> {
        return flow {
            wanAndroidApi.getBanner().data?.let {
                emit(it)
            }
        }
    }

    /**
     * 请求每日一问
     */
    fun getDailyQuestion(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false,
                pageSize = HOME_ARTICLE_PAGE_SIZE
            ),
            pagingSourceFactory = { DailyQuestionPagingSource(wanAndroidApi) }
        ).flow
    }

    /**
     * 请求广场数据
     */
    fun getSquareData(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false,
                pageSize = HOME_ARTICLE_PAGE_SIZE
            ),
            pagingSourceFactory = { SquarePagingSource(wanAndroidApi) }
        ).flow
    }


    /**
     * 请求项目分类
     */
    suspend fun loadProjectTree(): Flow<List<ProjectTree>>  {
        return flow {
            wanAndroidApi.loadProjectTree().data.let {
                emit(it)
            }
        }
    }

    /**
     * 通过项目分类的ID，利用Paging3+Flow请求项目详细列表。
     *
     */
    fun loadContentById(id: Int): Flow<PagingData<ProjectContent>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false,
                pageSize = PROJECT_CONTENT_PAGE_SIZE
            ),
            pagingSourceFactory = { ProjectDetailPagingSource(wanAndroidApi,id) }
        ).flow
    }

     suspend fun login(username: String, password: String): WanAndroidResponse<User> {
        Log.d("WanJetpackRepository", "login: name= $username , pass= $password")
         return  wanAndroidApi.login(username, password)
    }

    suspend fun logout(): WanAndroidResponse<User> {
        return  wanAndroidApi.logout()
    }

    suspend fun collect(id: Int):  WanAndroidResponse<Article> {
        return wanAndroidApi.collect(id)
    }

    suspend fun unCollect(id: Int):  WanAndroidResponse<Article> {
        return wanAndroidApi.unCollect(id)
    }

    suspend fun unCollectByCollection(id: Int,originId: Int):  WanAndroidResponse<Collect> {
        return wanAndroidApi.unCollectByCollection(id,originId)
    }


    fun getCollection(): Flow<PagingData<Collect>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 10,
                prefetchDistance = 5,
                enablePlaceholders = false,
                pageSize = HOME_ARTICLE_PAGE_SIZE
            ),
            pagingSourceFactory = { CollectionPagingSource(wanAndroidApi) }
        ).flow
    }
}