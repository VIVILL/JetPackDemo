package com.example.jetpackdemo

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.core.view.forEach
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.jetpackdemo.databinding.ActivityMainBinding
import com.example.jetpackdemo.viewmodel.UserViewModel
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MainActivity"

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val userViewModel: UserViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val name: String? = userViewModel.getLocalName()
        val password: String? = userViewModel.getLocalPassword()
        //自动登录
        if (userViewModel.getAutoLogin() && name != null && password != null){
            userViewModel.login(name, password)
        }

        initNavigation()
    }

    // https://stackoverflow.com/questions/58748117/android-leak-canary-leaking-empty-activity
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q &&
            isTaskRoot &&
            (supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.backStackEntryCount
                ?: 0) == 0 &&
            supportFragmentManager.backStackEntryCount == 0
        ) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * BottomNav显示控制
     */
    private var isShowBottomNav = true

    private fun initNavigation() {
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = host.navController
        // 移除 长按 toast
        binding.navigationView.menu.forEach {
            val menuItemView = findViewById<BottomNavigationItemView>(it.itemId)
            menuItemView.setOnLongClickListener(View.OnLongClickListener {
                return@OnLongClickListener true
            })
        }
        // BottomNavigationView 设置 navController
        binding.navigationView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            // 显示或隐藏导航
            when (destination.id) {
                R.id.homeFragment, R.id.projectFragment, R.id.userFragment -> {
                    showBottomNav()
                }
                else -> hideBottomNav()
            }
        }
    }

    /**
     * 显示导航
     */
    private fun showBottomNav() {
        Log.d(TAG,"inner showBottomNav")
        if (!isShowBottomNav) {
            binding.navigationView.visibility = View.VISIBLE
            isShowBottomNav = true
        }
    }

    /**
     * 隐藏导航
     */
    private fun hideBottomNav() {
        Log.d(TAG,"inner hideBottomNav")
        if (isShowBottomNav) {
            binding.navigationView.visibility = View.GONE
            isShowBottomNav = false
        }
    }

}