package com.example.jetpackdemo.fragment.project

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.ProjectTreeAdapter
import com.example.jetpackdemo.databinding.FragmentProjectBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.ProjectDetailViewModel
import com.example.jetpackdemo.viewmodel.ProjectViewModel
import com.example.jetpackdemo.viewmodel.UiAction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "ProjectFragment"

@AndroidEntryPoint
class ProjectFragment : Fragment() {
    private var _binding: FragmentProjectBinding? = null
    private val binding get() = _binding!!

    private val projectViewModel: ProjectViewModel by activityViewModels()
    private val projectDetailViewModel: ProjectDetailViewModel by activityViewModels()

    private val projectAdapter by lazy { ProjectTreeAdapter() }

    private var isFirstInit: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"inner onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentProjectBinding.inflate(inflater, container, false)
        projectAdapter.setOnItemClickListener { id , name ->
            Log.d(TAG,"project id = $id name = $name isFirstInit = $isFirstInit")
            if (isFirstInit){
                projectViewModel.setStartProjectId(id)
                isFirstInit = false
                Log.d(TAG,"isFirstInit = $isFirstInit")
            }
            projectDetailViewModel.loadProjectAction(UiAction.LoadProject(id = id))
            // 设置显示 DetailFragment
            val bundle = bundleOf( "title" to name)
            //跳转到带参数的 fragment
            findNavController().navigate(R.id.action_projectFragment_to_projectDetailFragment,bundle)
        }

        // 设置 adapter
        binding.recyclerview.adapter = projectAdapter

        // 下拉刷新 更新 项目分类
        binding.swipeLayout.setOnRefreshListener {
            // 更新 ProjectTree
            projectViewModel.loadProjectTreeList()

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

    override fun onDestroyView() {
        Log.d(TAG,"inner onDestroyView")
        super.onDestroyView()
        binding.recyclerview.adapter = null
        _binding = null
    }

    private fun subscribeUI() {
        lifecycleScope.launch(exceptionHandler) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                projectViewModel.projectTreeListStateFlow.collect{ projectTreeList ->
                    // 更新 数据
                    projectAdapter.submitList(projectTreeList)
                    //数据有更新时 停止显示 swipeLayout
                    binding.swipeLayout.isRefreshing = false

                }
            }
        }

    }

}