package com.example.jetpackdemo.fragment.project

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.FooterAdapter
import com.example.jetpackdemo.adapter.ProjectDetailAdapter
import com.example.jetpackdemo.databinding.FragmentProjectDetailBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.ProjectDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "ProjectDetailFragment"

@AndroidEntryPoint
class ProjectDetailFragment: Fragment() {
    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val projectDetailViewModel: ProjectDetailViewModel by activityViewModels()

    private val projectDetailAdapter by lazy { ProjectDetailAdapter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"inner onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentProjectDetailBinding.inflate(inflater, container, false)

        projectDetailAdapter.setOnItemClickListener { link, title ->
            Log.d(TAG,"project link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)

        }
        // 设置 adapter
        binding.recyclerview.adapter = projectDetailAdapter
            .withLoadStateFooter(FooterAdapter(projectDetailAdapter::retry))

        // 设置toolbar
        binding.toolbar.title = arguments?.getString("title")
        binding.toolbar.setNavigationOnClickListener { view ->
            findNavController().navigateUp()
        }

        // 下拉刷新 更新 项目
        binding.swipeLayout.setOnRefreshListener {
            // 更新
            projectDetailAdapter.refresh()
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
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                projectDetailViewModel.projectContent
                    .catch {
                        Log.d(TAG,"Exception : ${it.message}")
                    }
                    .collectLatest {
                        Log.d(TAG,"inner collectLatest")
                        // paging 使用 submitData 填充 adapter
                        projectDetailAdapter.submitData(it)
                    }
            }
        }

        //监听paging数据刷新状态
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                projectDetailAdapter.loadStateFlow.collectLatest {
                    Log.d(TAG, "inner collectLatest loadState = $it")
                    binding.swipeLayout.isRefreshing = it.refresh is LoadState.Loading
                }
            }
        }

    }

}