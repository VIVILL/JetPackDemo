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
import com.example.jetpackdemo.adapter.ViewPagerAdapter
import com.example.jetpackdemo.databinding.FragmentHomeBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.AutoScrollViewModel
import com.example.jetpackdemo.viewmodel.TouchAction
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val TAG = "HomeFragment"
@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val autoScrollViewModel: AutoScrollViewModel by activityViewModels()

    private var mLayoutMediator: TabLayoutMediator? = null

    lateinit var adapter: ViewPagerAdapter

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

        val fragmentStringList = listOf<String>(
            "HomePageArticleFragment",
            "DailyQuestionFragment",
            "SquareFragment"
        )

        adapter = ViewPagerAdapter(
            fragmentStringList,
            childFragmentManager,
            lifecycle
        )

        binding.viewPager2.adapter = adapter
        // 设置 offscreenPageLimit
        // 解决 使用 navigation 时 切换回 viewPager2 界面时内存泄漏问题
        binding.viewPager2.offscreenPageLimit = 1
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
        // TabLayout 解绑
        mLayoutMediator?.detach()
    }

}