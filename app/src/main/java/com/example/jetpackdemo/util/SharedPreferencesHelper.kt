package com.example.jetpackdemo.util

import android.content.Context
import android.content.SharedPreferences
import android.webkit.CookieManager
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.jetpackdemo.MainApplication
import okhttp3.Cookie
import java.lang.StringBuilder

//SharedPreferences模块封装
// https://www.jianshu.com/p/ed7c7d918497
object SharedPreferencesHelper {
    
    private fun getSharePref(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(MainApplication.instance)
    }

    fun put(key: String, value: String) {
        getSharePref().edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return getSharePref().getString(key, "")
    }

    fun put(key: String, value: Boolean) {
        getSharePref().edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return getSharePref().getBoolean(key, false)
    }


    private fun getEncryptedSharePref(): SharedPreferences {
        return EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            MainApplication.instance,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun putPassWord(key: String, value: String) {
        getEncryptedSharePref().edit().putString(key, value).apply()
    }

    fun getPassWord(key: String): String? {
        return getEncryptedSharePref().getString(key, "")
    }





    private fun getCookieSharePref(): SharedPreferences {
        return MainApplication.instance.getSharedPreferences("cookies", Context.MODE_PRIVATE)
    }

    fun saveCookies(cookies: List<Cookie>) {
        val editor = getCookieSharePref().edit()
        val cookie = StringBuilder()
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookies.forEach {
            editor.putString(it.name, it.value)
            cookie.append(it.name).append("=").append(it.value).append(";")
            cookieManager.setCookie(it.domain, "${it.name}=${it.value}")
        }
        editor.apply()
        cookieManager.flush()
    }

    /**
     * 生成的cookies比服务器传进来的cookies少了secure、httponly、expires
     */
    fun getCookies(): ArrayList<Cookie> {
        val sp = getCookieSharePref()
        return ArrayList<Cookie>().apply {
            val names = sp.all.keys
            names.forEach {
                add(
                    Cookie.Builder()
                        .domain("www.wanandroid.com")
                        .name(it)
                        .value(sp.getString(it,"") ?: "")
                        .build()
                )
            }
        }
    }
}