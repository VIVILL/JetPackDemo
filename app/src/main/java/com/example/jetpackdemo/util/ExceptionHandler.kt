package com.example.jetpackdemo.util

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

private const val TAG = "ExceptionHandler"

object ExceptionHandler {
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.e(TAG, "CoroutineExceptionHandler exception : ${exception.message}")
    }
}