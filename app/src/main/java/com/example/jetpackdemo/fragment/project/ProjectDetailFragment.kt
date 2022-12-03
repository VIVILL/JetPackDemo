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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.FooterAdapter
import com.example.jetpackdemo.adapter.ProjectDetailAdapter
import com.example.jetpackdemo.databinding.FragmentProjectDetailBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.ProjectViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "ProjectContentFragment"

@AndroidEntryPoint
class ProjectDetailFragment: Fragment() {
    private var _binding: FragmentProjectDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProjectViewModel by activityViewModels()
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
            viewModel.loadProjectContent()

            viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    //数据无更新时 一段时间后停止显示 swipeLayout
                    delay(2000L)
                    binding.swipeLayout.isRefreshing = false
                }
            }
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
                viewModel.projectContentFlow
                    .catch {
                        Log.d(TAG,"Exception : ${it.message}")
                    }
                    .collectLatest {
                        Log.d(TAG,"inner collectLatest")
                        // paging 使用 submitData 填充 adapter
                        projectDetailAdapter.submitData(it)
                        //停止显示 swipeLayout
                        binding.swipeLayout.isRefreshing = false
                    }
            }
        }

    }

}