package com.example.jetpackdemo.fragment.home.viewpager

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.*
import com.example.jetpackdemo.databinding.FragmentHomePageBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



private const val TAG = "HomePageArticleFragment"
@AndroidEntryPoint
class HomePageFragment: Fragment() {
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private val homePageViewModel: HomePageViewModel by activityViewModels()
    private val collectViewModel: CollectViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val autoScrollViewModel: AutoScrollViewModel by activityViewModels()

    private val headerItemAdapter by lazy {
        HomePageHeaderItemAdapter(listOf()){link, title ->
            Log.d(TAG,"link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)
        }
    }

    private val headerAdapter by lazy {
        HomePageHeaderAdapter(headerItemAdapter)
    }

    private val articleAdapter by lazy {
        HomePageBodyAdapter()
    }
    private lateinit var concatAdapter: ConcatAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"inner onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"inner onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentHomePageBinding.inflate(inflater, container, false)

        headerAdapter.setOnViewAttachedListener {
            Log.d(TAG, "viewModel.autoScroll()")
            // 延时3s后 开启自动滑动
            viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    delay(3000L)
                    autoScrollViewModel.autoScroll()
                }
            }
        }
        headerAdapter.setOnViewDetachedListener {
            Log.d(TAG, "viewModel.cancelAutoScroll()")
            autoScrollViewModel.cancelAutoScroll()
        }
        headerAdapter.setTouchEventListener { motionEvent ->
            Log.d(TAG, "inner touch motionEvent = $motionEvent")
            autoScrollViewModel.touchViewPager2(motionEvent)
        }

        articleAdapter.setOnItemClickListener { link, title ->
            Log.d(TAG,"link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)
        }
        articleAdapter.setImageViewClickListener { id,collect->
            // 如果是已收藏状态 就取消收藏 如果是未收藏状态则 收藏
            Log.d(TAG,"inner setImageViewClickListener collect = $collect")
            if (collect){
                collectViewModel.unCollect(id)
            }else {
                collectViewModel.collect(id)
            }
        }

        concatAdapter = articleAdapter.withLoadStateFooter(FooterAdapter(articleAdapter::retry))
        concatAdapter.addAdapter(0,headerAdapter)
        // 为RecyclerView配置adapter
        binding.recyclerview.adapter = concatAdapter
        // 设置需要缓存的 ViewHolder数量 防止离屏后再显示时 频繁执行 onBindViewHolder
        binding.recyclerview.setItemViewCacheSize(10)


        binding.swipeLayout.setOnRefreshListener {
            Log.d(TAG,"inner setOnRefreshListener")
            // 刷新时 重新加载数据
            homePageViewModel.loadBanner()
            // 更新 PagingDataAdapter
            articleAdapter.refresh()
        }
        binding.recyclerview.setTouchEventListener{
            Log.d(TAG,"inner setTouchEventListener")
            autoScrollViewModel.touchRecyclerview(it)
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    override fun onDestroyView() {
        Log.d(TAG,"inner onDestroyView")
        super.onDestroyView()
        binding.recyclerview.adapter = null
        _binding = null
    }

    private fun subscribeUI() {
        // banner
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                homePageViewModel.banner.collect{ value ->
                    Log.d(TAG," value size = ${value.size}")
                    // 更新 list
                    headerItemAdapter.bannerList = value
                    Log.d(TAG," bannerList size = ${headerItemAdapter.bannerList.size}")
                    //数据改变刷新视图
                    headerAdapter.notifyItemChanged(0)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                autoScrollViewModel.autoScrollAction.collect {
                    when (it) {
                        is AutoScrollAction.AutoScroll -> {
                            Log.d(TAG, "inner AutoScroll")
                            // 通过设置动画，实现 自动滑动时 平滑滚动
                            headerAdapter.setCurrentItem()
                            Log.d(TAG, "after setCurrentItem position duration 300")

                        }
                    }

                }

            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler){
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                //监听 PagingData
                homePageViewModel.homePageArticleFlow
                    .catch {
                        Log.d(TAG,"Exception : ${it.message}")
                    }
                    .collectLatest {
                        Log.d(TAG,"inner collectLatest")
                        // paging 使用 submitData 填充 adapter
                        articleAdapter.submitData(it)
                    }
            }
        }

        //监听paging数据刷新状态
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                articleAdapter.loadStateFlow.collectLatest {
                    Log.d(TAG, "inner collectLatest loadState = $it")
                    binding.swipeLayout.isRefreshing = it.refresh is LoadState.Loading
                }
            }
        }

        // 收藏
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectViewModel.collectAction.collect {
                    when (it) {
                        is CollectAction.Success -> {
                            Log.d(TAG,"CollectAction.Success")
                            articleAdapter.refresh()

                        }
                        is CollectAction.Error -> {
                            articleAdapter.refresh()
                        }
                        else -> {}
                    }
                }
            }
        }

        // 取消收藏
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectViewModel.unCollectAction.collect {
                    when (it) {
                        is UnCollectAction.Success -> {
                            Log.d(TAG,"UnCollectAction.Success")
                            articleAdapter.refresh()
                        }
                        is UnCollectAction.Error -> {
                            articleAdapter.refresh()
                        }

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.loginUiState.collect {
                    //自动刷新
                    articleAdapter.refresh()
                    Log.d(TAG, "after articleAdapter.refresh")
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = HomePageFragment()
    }

}