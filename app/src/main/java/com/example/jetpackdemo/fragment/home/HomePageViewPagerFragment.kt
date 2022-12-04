package com.example.jetpackdemo.fragment.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jetpackdemo.adapter.HomePageViewPagerAdapter
import com.example.jetpackdemo.databinding.FragmentHomeBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.AutoScrollViewModel
import com.example.jetpackdemo.viewmodel.TouchAction
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

//参考链接
// FragmentStatePagerAdapter使用不当引起的内存泄漏问题 https://blog.csdn.net/TE28093163/article/details/122992737
// Android Navigation 遇坑记 - 真实项目经历 https://juejin.cn/post/7002798538484613127

/**
 * 使用 viewPager2 有几个点需要注意：
 * 1. Fragment的创建要使用单例，并且不能直接往 adapter传入 fragment
 * 2. navigation + viewPager2 + recyclerview 界面切换时 recyclerview内存泄漏 具体可参考： https://issuetracker.google.com/issues/154751401
 * 3. LayoutMediator 要解绑，并置 null
 * https://stackoverflow.com/questions/61779776/leak-canary-detects-memory-leaks-for-tablayout-with-viewpager2
 * 4. _binding 和 viewPager2 的 adapter 要置 null
 * */
private const val TAG = "HomeFragment"
@AndroidEntryPoint
class HomePageViewPagerFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val autoScrollViewModel: AutoScrollViewModel by activityViewModels()

    private var mLayoutMediator: TabLayoutMediator? = null

    lateinit var adapter: HomePageViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"inner onCreate")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       // return inflater.inflate(R.layout.fragment_home, container, false)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d(TAG,"inner onCreateView")

        // 不能直接传入fragment到 FragmentStateAdapter，否则会存在内存泄漏，应传入string，在 FragmentStateAdapter 中使用单例创建fragment
        val fragmentStringList = listOf<String>(
            "HomePageArticleFragment",
            "DailyQuestionFragment",
            "SquareFragment"
        )
// https://issuetracker.google.com/issues/154751401
// 解决 使用 navigation + viewPager2 + recyclerview 界面切换时内存泄漏问题 注意点：
// 1.使用viewLifecycleOwner.lifecycle 而不是 lifecycle
// 2. recyclerview的adapter 在onDestroyView 中置 null
        adapter = HomePageViewPagerAdapter(
            fragmentStringList,
            childFragmentManager,
            //lifecycle
            viewLifecycleOwner.lifecycle
        )

        binding.viewPager2.adapter = adapter
        // 设置 offscreenPageLimit
        binding.viewPager2.offscreenPageLimit = fragmentStringList.size -1
        //绑定 tabLayout 和viewPager
        mLayoutMediator =  TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager2
        ) { tab, position ->
            when (position) {
                0 -> tab.text = "首页"
                1 -> tab.text = "每日一问"
                else -> tab.text = "广场"
            }
        }
        mLayoutMediator?.attach()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    override fun onDestroyView() {
        Log.d(TAG,"inner onDestroyView")
        super.onDestroyView()
        // https://stackoverflow.com/questions/61779776/leak-canary-detects-memory-leaks-for-tablayout-with-viewpager2
        // TabLayout 解绑
        mLayoutMediator?.detach()
        mLayoutMediator = null
        binding.viewPager2.adapter = null
        _binding = null
    }

    private var startX = 0
    private var startY = 0

    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler){
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                autoScrollViewModel.recyclerviewTouchAction.collect {
                    when (it) {
                        is TouchAction.Touch -> {
                            Log.d(TAG, "inner Touch")
                            when (it.event.action) {
                                MotionEvent.ACTION_DOWN -> {
                                    Log.d(TAG, "inner MotionEvent.ACTION_DOWN")
                                    startX = it.event.x.toInt()
                                    startY = it.event.y.toInt()
                                    Log.d(TAG,"ACTION_DOWN startX = $startX startY = $startY ")
                                }
                                MotionEvent.ACTION_MOVE -> {
                                    val endX = it.event.x.toInt()
                                    val endY = it.event.y.toInt()
                                    val disX = abs(endX - startX)
                                    val disY = abs(endY - startY)
                                    if (disX < disY) {
                                        binding.viewPager2.isUserInputEnabled = false
                                        Log.d(TAG, "inner ACTION_MOVE binding.viewPager2.isUserInputEnabled = false")

                                    }
                                }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL-> {
                                    Log.d(TAG, "inner MotionEvent.ACTION_UP or ACTION_CANCEL binding.viewPager2.isUserInputEnabled = true")
                                    startX = 0
                                    startY = 0
                                    binding.viewPager2.isUserInputEnabled = true
                                }

                                else -> {}
                            }
                        }

                    }
                }
            }
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG,"inner onDestroy")
    }

}