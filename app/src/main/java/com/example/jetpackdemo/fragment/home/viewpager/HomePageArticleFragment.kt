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
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.HeaderItemAdapter
import com.example.jetpackdemo.adapter.FooterAdapter
import com.example.jetpackdemo.adapter.HeaderAdapter
import com.example.jetpackdemo.adapter.HomePageArticleAdapter
import com.example.jetpackdemo.bean.Banner
import com.example.jetpackdemo.databinding.FragmentHomePageBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "HomePageArticleFragment"
@AndroidEntryPoint
class HomePageArticleFragment() : Fragment() {
    private var _binding: FragmentHomePageBinding? = null
    private val binding get() = _binding!!

    private val wanAndroidViewModel: WanAndroidViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val homePageArticleViewModel: HomePageArticleViewModel by viewModels()

    private val headerItemAdapter by lazy {
        HeaderItemAdapter(listOf()){link, title ->
            Log.d(TAG,"link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)
        }
    }

    private val headerAdapter by lazy {
        HeaderAdapter(headerItemAdapter)
    }

    private val articleAdapter by lazy {
        HomePageArticleAdapter()
    }
    private lateinit var concatAdapter: ConcatAdapter


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"inner onCreate")
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
            wanAndroidViewModel.autoScroll()
        }
        headerAdapter.setOnViewDetachedListener {
            Log.d(TAG, "viewModel.cancelAutoScroll()")
            wanAndroidViewModel.cancelAutoScroll()
        }
        headerAdapter.setTouchEventListener { motionEvent ->
            Log.d(TAG, "inner touch motionEvent = $motionEvent")
            wanAndroidViewModel.touchViewPager2(motionEvent)
        }

        // 解决RecyclerView刷新局部Item闪烁
        // 设置为True 不走重绘逻辑
        binding.recyclerview.setHasFixedSize(true)
        (binding.recyclerview.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false

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
                wanAndroidViewModel.unCollect(id)
            }else {
                wanAndroidViewModel.collect(id)
            }
        }

        concatAdapter = articleAdapter.withLoadStateFooter(FooterAdapter(articleAdapter::retry))
        concatAdapter.addAdapter(0,headerAdapter)
        // 为RecyclerView配置adapter
        binding.recyclerview.adapter = concatAdapter
        // 设置需要缓存的 ViewHolder数量 防止离屏后再显示时 频繁执行 onBindViewHolder
        binding.recyclerview.setItemViewCacheSize(10)

        // 更新 Banner
        wanAndroidViewModel.updateBannerList()

        binding.swipeLayout.setOnRefreshListener {
            // 刷新时 更新 Banner
            wanAndroidViewModel.updateBannerList()
            // 更新 PagingDataAdapter
            articleAdapter.refresh()
        }
        binding.recyclerview.setTouchEventListener{
            Log.d(TAG,"inner setTouchEventListener")
            wanAndroidViewModel.touchRecyclerview(it)
        }
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    private fun subscribeUI() {
        // bannerListFlow
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                wanAndroidViewModel.bannerListStateFlow.collect{ value ->
                    Log.d(TAG," value size = ${value.size}")
                    // 将获取的数据 进行拼接处理
                    val list = ArrayList<Banner>()
                    if (value.isNotEmpty()){
                        list.add(value[value.size-1])
                        list.addAll(value)
                        list.add(value[0])
                        Log.d(TAG," list size = ${list.size} list = $list")
                    }
                    
                    // 更新 list
                    headerItemAdapter.bannerList = list
                    Log.d(TAG," bannerList size = ${headerItemAdapter.bannerList.size}")
                    //数据改变刷新视图
                    headerAdapter.notifyItemChanged(0)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                wanAndroidViewModel.autoScrollAction.collect {
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
                // 获取 PagingData
                homePageArticleViewModel.homePageArticleFlow
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
                wanAndroidViewModel.collectAction.collect {
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
                wanAndroidViewModel.unCollectAction.collect {
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
                    // 处于登录状态时 显示退出按钮 和user名
                    if (it.loginState == 0){
                        Log.d(TAG,"user username = ${it.user?.username}")
                        // 登录后自动刷新下
                       articleAdapter.refresh()
                    }
                }
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomePageArticleFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}