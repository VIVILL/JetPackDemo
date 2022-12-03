package com.example.jetpackdemo.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.bean.User
import com.example.jetpackdemo.bean.WanAndroidResponse
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.util.SharedPreferencesHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


private const val TAG = "LoginViewModel"

// Hilt 提供了一些预定义的限定符。例如，由于您可能需要来自应用或 activity 的 Context 类，
// 因此 Hilt 提供了 @ApplicationContext 和 @ActivityContext 限定符。
// https://developer.android.com/training/dependency-injection/hilt-android?hl=zh-cn
//  @Provides
//    fun provideContext(@ApplicationContext context: Context): Context {
//        return context
//    }
// @Inject constructor(private val application: Application)
// https://stackoverflow.com/questions/66216839/inject-context-with-hilt-this-field-leaks-a-context-object
@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: WanAndroidRepository,
    application: Application
) : AndroidViewModel(application){

    init {
        Log.d(TAG,"inner init")
    }

/*
    var stateChanged: Boolean = false
*/

    lateinit var user: User

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState(-1,null))

    val loginUiState: StateFlow<LoginUiState> = _loginUiState

    // 更新 登录状态

    private fun updateLoginUiState(loginState: Int,user: User?) {
        viewModelScope.launch(exceptionHandler) {
            _loginUiState.value = LoginUiState(loginState,user)
        }
    }


    // 使用 SharedFlow
    private val _loginUiAction = MutableSharedFlow<LoginUiAction>()
    val loginUiAction: SharedFlow<LoginUiAction> = _loginUiAction

    fun login(userName: String, password: String) {
        viewModelScope.launch(exceptionHandler) {
            _loginUiAction.emit(LoginUiAction.Loading)
            try{
                val response: WanAndroidResponse<User> = repository.login(userName,password)
                if(response.errorCode == 0) {
                    Log.d(TAG,"login, response.errorCode = 0")
                    _loginUiAction.emit(LoginUiAction.Success)
                    // 登录成功  初始化 user
                    user = response.data
                    Log.d(TAG,"user username = ${user.username}")
                   /* stateChanged = true
                    Log.d(TAG,"stateChanged = $stateChanged")*/
                    updateLoginUiState(0,user)
                } else if(response.errorCode == -1){
                    // "data": null,
                    // "errorCode": -1,
                    // "errorMsg": "账号密码不匹配！"
                    _loginUiAction.emit(LoginUiAction.Error("账号密码不匹配！"))
                }
            }catch (e: Exception){
                e.printStackTrace()
                _loginUiAction.emit(LoginUiAction.Error("登录失败"))
            }

        }
    }


    private val _logout = MutableSharedFlow<LogoutUiAction>()

    val logout: SharedFlow<LogoutUiAction> = _logout

    fun logout() {
        viewModelScope.launch(exceptionHandler) {
            try{
                val response: WanAndroidResponse<User> = repository.logout()
                if(response.errorCode == 0) {
                    _logout.emit(LogoutUiAction.Success)
                    // 退出后 更新登录状态
                   /* stateChanged = true
                    Log.d(TAG,"stateChanged = $stateChanged")*/
                    updateLoginUiState(-1,user)
                }
            }catch (e: Exception){
                e.printStackTrace()
                _logout.emit(LogoutUiAction.Error("退出失败"))
            }
        }
    }


    fun getRememberAccount(): Boolean {
        return SharedPreferencesHelper.getBoolean("rememberAccount")
    }

    fun setRememberAccountSP(shouldRememberAccount: Boolean) {
        SharedPreferencesHelper.put("rememberAccount",shouldRememberAccount)
    }

    fun getAutoLogin(): Boolean {
        return SharedPreferencesHelper.getBoolean("autoLogin")
    }

    fun setAutoLoginSP(shouldAutoLogin: Boolean) {
        SharedPreferencesHelper.put("autoLogin",shouldAutoLogin)
    }

    /**
     * 保存用户账号
     */
    fun setUserNameSP(userName: String) {
        SharedPreferencesHelper.put("name",userName)
    }

    /**
     * 保存用户密码
     */
    fun setUserPasswordSP(password: String) {
        SharedPreferencesHelper.putPassWord("password",password)
    }

    /**
     * 获得保存在本地的用户名
     */
    fun getLocalName(): String?{
        return SharedPreferencesHelper.getString("name")
    }

    /**
     * 获得保存在本地的密码
     */
    fun getLocalPassword(): String? {
        return SharedPreferencesHelper.getPassWord("password")
    }

}

sealed class LoginUiAction {
    object Success : LoginUiAction()
    data class Error(val message: String) : LoginUiAction()
    object Loading : LoginUiAction()
}

sealed class LogoutUiAction {
    object Success : LogoutUiAction()
    data class Error(val message: String) : LogoutUiAction()
    object Loading : LogoutUiAction()
}

data class LoginUiState(val loginState: Int, val user: User?)
