package com.example.jetpackdemo.fragment.user

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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.CollectionAdapter
import com.example.jetpackdemo.adapter.FooterAdapter
import com.example.jetpackdemo.databinding.FragmentCollectionBinding
import com.example.jetpackdemo.util.ExceptionHandler
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.CollectViewModel
import com.example.jetpackdemo.viewmodel.UnCollectAction
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "CollectionFragment"
@AndroidEntryPoint
class CollectionFragment : Fragment() {
    private var _binding: FragmentCollectionBinding? = null
    private val binding get() = _binding!!

    private val collectViewModel: CollectViewModel by viewModels()

    private val collectionAdapter by lazy {
        CollectionAdapter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"inner onCreateView")
       _binding = FragmentCollectionBinding.inflate(inflater, container, false)

        // 设置toolbar
        binding.toolbar.title = "我的收藏"
        binding.toolbar.setNavigationOnClickListener { view ->
            findNavController().navigateUp()
        }

        binding.recyclerview.setHasFixedSize(true)
        (binding.recyclerview.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false

        collectionAdapter.setOnItemClickListener { link, title ->
            Log.d(TAG,"link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)
        }
        collectionAdapter.setImageViewClickListener { id,originId->
            // 取消收藏
            Log.d(TAG,"id = $id originId = $originId")
            collectViewModel.unCollectByCollection(id,originId)

        }

        val concatAdapter = collectionAdapter.withLoadStateFooter(FooterAdapter(collectionAdapter::retry))
        // 为RecyclerView配置adapter
        binding.recyclerview.adapter = concatAdapter

        binding.swipeLayout.setOnRefreshListener {
            // 更新 PagingDataAdapter
            collectionAdapter.refresh()
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 获取 PagingData
                collectViewModel.getCollection()
                    .catch {
                        Log.d(TAG,"Exception : ${it.message}")
                    }
                    .collectLatest {
                        Log.d(TAG,"inner collectLatest")
                        // paging 使用 submitData 填充 adapter
                        collectionAdapter.submitData(it)
                    }
            }
        }
        //监听paging数据刷新状态
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectionAdapter.loadStateFlow.collectLatest {
                    Log.d(TAG, "inner collectLatest loadState = $it")
                    binding.swipeLayout.isRefreshing = it.refresh is LoadState.Loading
                }
            }
        }

        // 取消收藏
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                collectViewModel.unCollectByCollectionAction.collect {
                    when (it) {
                        is UnCollectAction.Success -> {
                            Log.d(TAG,"UnCollectAction.Success")
                            Snackbar.make(
                                binding.root,
                                it.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            // 删除取消收藏的数据
                            collectionAdapter.refresh()
                        }
                        is UnCollectAction.Error -> {
                            Snackbar.make(
                                binding.root,
                                it.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            collectionAdapter.refresh()
                        }

                    }
                }
            }
        }


    }


}