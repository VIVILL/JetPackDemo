package com.example.jetpackdemo.api

import android.util.Log
import com.example.jetpackdemo.bean.*
import com.example.jetpackdemo.util.SharedPreferencesHelper
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

//api 接口说明 https://www.wanandroid.com/blog/show/2
interface WanAndroidApi {

    companion object {
        private const val BASE_URL = "https://www.wanandroid.com/"

        fun create(): WanAndroidApi {
            val logger = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .cookieJar(LocalCookie())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WanAndroidApi::class.java)
        }
    }



    // https://www.wanandroid.com/article/list/0/json
    /**
     * 获取文章
     */
    @GET("/article/list/{page}/json")
    suspend fun getHomePageArticle(
        @Path("page") page: Int
    ): WanAndroidResponse<BasePageData<Article>>


    // https://www.wanandroid.com/banner/json
    /**
     * Banner
     */
    @GET("banner/json")
    suspend fun getBanner(): BannerResponse<List<Banner>>

    // https://wanandroid.com/wenda/list/1/json
    @GET("wenda/list/{page}/json")
    suspend fun getDailyQuestion(
        @Path("page") page: Int
    )
    : WanAndroidResponse<BasePageData<Article>>

    // https://wanandroid.com/user_article/list/0/json
    @GET("user_article/list/{page}/json")
    suspend fun getSquareData(@Path("page") page: Int)
    : WanAndroidResponse<BasePageData<Article>>


/** 注册 https://www.wanandroid.com/user/register
    方法：POST   参数  username,password,repassword
 */

    @POST("/user/register")
    suspend fun register(
         @Query("username") username: String,
         @Query("password") password: String,
         @Query("repassword") repassword: String
    ): WanAndroidResponse<User>

    /**
     *
     * https://www.wanandroid.com/user/login
    方法：POST
    参数：
    username，password
     * */
    @POST("/user/login")
    suspend fun login(
        @Query("username") username: String,
        @Query("password") password: String
    ): WanAndroidResponse<User>

    // https://www.wanandroid.com/user/logout/json
    /**
    * {
    "data": null,
    "errorCode": 0,
    "errorMsg": ""}
    * */
    @GET("/user/logout/json")
    suspend fun logout(): WanAndroidResponse<User>

    //返回整个分类 https://www.wanandroid.com/project/tree/json
    @GET("project/tree/json")
    suspend fun loadProjectTree(): WanAndroidResponse<List<ProjectTree>>

    //项目列表数据 https://www.wanandroid.com/project/list/1/json?cid=294
    @GET("project/list/{path}/json")
    suspend fun loadContentById(
        @Path("path") path: Int,
        @Query("cid") cid: Int
    ): WanAndroidResponse<BasePageData<ProjectContent>>

    // 收藏相关都需要登录操作，建议登录将返回的cookie（其中包含账号、密码）持久化到本地即可
    // 如果不做持久化，不管当前应用是否登录，此处返回的都是errorCode=-1001。即认为没有登录。
    // https://www.wanandroid.com/lg/collect/1165/json
    @POST("lg/collect/{id}/json")
    suspend fun collect(
        @Path("id") id: Int
    ): WanAndroidResponse<Article>

    
    @POST("lg/uncollect_originId/{id}/json")
    suspend fun unCollect(
        @Path("id") id: Int
    ): WanAndroidResponse<Article>

    // https://www.wanandroid.com/lg/collect/list/0/json
    /**
     * 获取收藏列表
     */
    @GET("lg/collect/list/{page}/json")
    suspend fun getCollection(
        @Path("page") page: Int
    ): WanAndroidResponse<BasePageData<Collect>>


    // https://www.wanandroid.com/lg/uncollect/285046/json?&originId=24989
    /*
    "data": null,
    "errorCode": 0,
    "errorMsg": ""
    * */
    @POST("lg/uncollect/{id}/json")
    suspend fun unCollectByCollection(
        @Path("id") id: Int,
        @Query("originId") originId: Int,
    ): WanAndroidResponse<Collect>
}

/**
 * Cookie 本地化方案
 */
class LocalCookie : CookieJar {
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookies = SharedPreferencesHelper.getCookies()
        Log.d("LocalCookie", "loadForRequest: ljh cookies=$cookies")
        return cookies
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        Log.d("LocalCookie", "saveFromResponse: ljh cookies=$cookies")
        //如果注册新账号之后没有重新登录，则需要在注册时也要saveCookies。退出登录时，要通过saveCookies重置。
        val isSaveCookies = url.toString().startsWith("https://www.wanandroid.com/user/login?")
                || url.toString().startsWith("https://www.wanandroid.com/user/register?")
                || url.toString().startsWith("https://www.wanandroid.com/user/logout/json")
        if (isSaveCookies) {
            SharedPreferencesHelper.saveCookies(cookies)
        }
    }
}
