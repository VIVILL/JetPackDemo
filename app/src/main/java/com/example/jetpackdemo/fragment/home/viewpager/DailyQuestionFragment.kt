package com.example.jetpackdemo.fragment.home.viewpager

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
import androidx.paging.LoadState
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.jetpackdemo.R
import com.example.jetpackdemo.adapter.DailyQuestionAdapter
import com.example.jetpackdemo.adapter.FooterAdapter
import com.example.jetpackdemo.databinding.FragmentDailyQuestionBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.CollectAction
import com.example.jetpackdemo.viewmodel.UnCollectAction
import com.example.jetpackdemo.viewmodel.WanAndroidViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private const val TAG = "DailyQuestionFragment"
@AndroidEntryPoint
class DailyQuestionFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentDailyQuestionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WanAndroidViewModel by activityViewModels()
    private val dailyQuestionAdapter by lazy{
        DailyQuestionAdapter()
    }

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
        _binding = FragmentDailyQuestionBinding.inflate(inflater, container, false)

        // 解决RecyclerView刷新局部Item闪烁
        // 设置为True 不走重绘逻辑
        binding.recyclerview.setHasFixedSize(true)
        (binding.recyclerview.itemAnimator as SimpleItemAnimator?)!!.supportsChangeAnimations = false

        dailyQuestionAdapter.setOnItemClickListener { link, title ->
            Log.d(TAG,"link = $link title = $title")
            val navController = findNavController()
            // 跳转到 WebFragment
            val bundle = bundleOf("link" to link, "title" to title)
            //跳转到带参数的 fragment
            navController.navigate(R.id.webFragment,bundle)
        }
        dailyQuestionAdapter.setImageViewClickListener{ id,collect ,position->
            // 如果是已收藏状态 就取消收藏 如果是未收藏状态则 收藏
            Log.d(TAG,"collect = $collect position = $position")
            if (collect){
                viewModel.unCollect(id,position)
            }else {
                viewModel.collect(id,position)
            }
        }
        // 为RecyclerView配置adapter
        binding.recyclerview.adapter = dailyQuestionAdapter
            .withLoadStateFooter(FooterAdapter(dailyQuestionAdapter::retry))

        binding.swipeLayout.setOnRefreshListener {
            // 更新 PagingDataAdapter
            dailyQuestionAdapter.refresh()
        }

        binding.recyclerview.setTouchEventListener{
            Log.d(TAG,"inner setTouchEventListener")
            viewModel.touchRecyclerview(it)
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    private fun subscribeUI(){
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 获取 PagingData
                viewModel.getDailyQuestion()
                    .catch {
                        Log.d(TAG,"Exception : ${it.message}")
                    }
                    .collectLatest {
                        Log.d(TAG,"inner collectLatest")
                        // paging 使用 submitData 填充 adapter
                        dailyQuestionAdapter.submitData(it)
                    }
            }
        }

        //监听paging数据刷新状态
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dailyQuestionAdapter.loadStateFlow.collectLatest {
                    Log.d(TAG, "inner collectLatest loadState = $it")
                    binding.swipeLayout.isRefreshing = it.refresh is LoadState.Loading
                }
            }
        }

        // 收藏
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.collectAction.collect {
                    when (it) {
                        is CollectAction.Success -> {
                            Log.d(TAG,"CollectAction.Success")
                            dailyQuestionAdapter.refresh()
                        }
                        is CollectAction.Error -> {
                            dailyQuestionAdapter.refresh()
                        }

                    }
                }
            }
        }

        // 取消收藏
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.unCollectAction.collect {
                    when (it) {
                        is UnCollectAction.Success -> {
                            Log.d(TAG,"UnCollectAction.Success")
                            dailyQuestionAdapter.refresh()
                        }
                        is UnCollectAction.Error -> {
                            dailyQuestionAdapter.refresh()
                        }

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
         * @return A new instance of fragment SecondFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DailyQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}