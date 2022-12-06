package com.example.jetpackdemo.fragment.home.viewpager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.FooterAdapter
import com.example.jetpackdemo.adapter.SquareAdapter
import com.example.jetpackdemo.databinding.FragmentSquareBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch



private const val TAG = "SquareFragment"
@AndroidEntryPoint
class SquareFragment : Fragment() {
    private var _binding: FragmentSquareBinding? = null
    private val binding get() = _binding!!

    private val homePageViewModel: HomePageViewModel by activityViewModels()
    private val collectViewModel: CollectViewModel by viewModels()
    private val userViewModel: UserViewModel by activityViewModels()
    private val autoScrollViewModel: AutoScrollViewModel by activityViewModels()

    private val squareAdapter by lazy {
        SquareAdapter()
    }

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
        _binding = FragmentSquareBinding.inflate(inflater, container, false)

        squareAdapter.setOnItemClickListener { link, title ->
            Log.d(TAG,"link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)
        }
        squareAdapter.setImageViewClickListener { id,collect->
            // 如果是已收藏状态 就取消收藏 如果是未收藏状态则 收藏
            Log.d(TAG,"collect = $collect")
            if (collect){
                collectViewModel.unCollect(id)
            }else {
                collectViewModel.collect(id)
            }
        }

        val concatAdapter =  squareAdapter.withLoadStateFooter(
            FooterAdapter(squareAdapter::retry))
        // 为RecyclerView配置adapter
        binding.recyclerview.adapter = concatAdapter

        binding.swipeLayout.setOnRefreshListener {
            // 更新 PagingDataAdapter
            squareAdapter.refresh()
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

    private fun subscribeUI(){
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 监听 PagingData
                homePageViewModel.squareDataFlow
                    .catch {
                        Log.d(TAG,"Exception : ${it.message}")
                    }
                    .collectLatest {
                        Log.d(TAG,"inner collectLatest")
                        // paging 使用 submitData 填充 adapter
                        squareAdapter.submitData(it)
                    }
            }
        }

        //监听paging数据刷新状态
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                squareAdapter.loadStateFlow.collectLatest {
                    Log.d(TAG, "inner collectLatest loadState = $it")
                    binding.swipeLayout.isRefreshing = it.refresh is LoadState.Loading
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler){
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectViewModel.collectAction.collect {
                    when (it) {
                        is CollectAction.Success -> {
                            Log.d(TAG,"CollectAction.Success")
                            // 局部刷新
                            squareAdapter.refresh()
                        }
                        is CollectAction.Error -> {
                            squareAdapter.refresh()
                        }

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
                            squareAdapter.refresh()
                        }
                        is UnCollectAction.Error -> {
                            squareAdapter.refresh()
                        }

                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.squareState.collect {
                    Log.d(TAG, "inner squareState collect it = $it")
                    when (it) {
                        is StateUiAction.StateChanged -> {
                            Log.d(TAG,"inner StateChanged")
                            //自动刷新
                            squareAdapter.refresh()
                            Log.d(TAG, "after squareAdapter.refresh")
                            userViewModel.resetSquareState()
                        }
                        else -> {}
                    }
                }
            }
        }
    }


    companion object {
        @JvmStatic
        fun newInstance() = SquareFragment()
    }
}