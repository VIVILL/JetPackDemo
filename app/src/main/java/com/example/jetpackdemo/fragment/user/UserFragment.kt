package com.example.jetpackdemo.fragment.user

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jetpackdemo.R
import com.example.jetpackdemo.databinding.FragmentUserBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.LogoutUiAction
import com.example.jetpackdemo.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


private const val TAG = "UserFragment"

@AndroidEntryPoint
class UserFragment : Fragment() {

    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!

    // fragment 之间 共享 UserViewModel
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"inner onCreateView")
        // Inflate the layout for this fragment
        _binding = FragmentUserBinding.inflate(inflater, container, false)

        binding.userImage.setOnClickListener {
            Log.d(TAG,"inner userLoginText OnClickListener")
            if (findNavController().currentDestination?.id == R.id.userFragment) {
                if(viewModel.loginUiState.value.loginState == 0){
                    Snackbar.make(it, "进入个人信息界面，待实现", Snackbar.LENGTH_LONG).show()
                }else{
                    // 跳转至 登录界面
                    findNavController().navigate(R.id.action_userFragment_to_loginFragment)
                }
            }
        }

        binding.collectTextView.setOnClickListener {
            // 跳转至 我的收藏
            findNavController().navigate(R.id.action_userFragment_to_collectionFragment)

        }

        binding.logoutButton.setOnClickListener {
            viewModel.logout()
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
        _binding = null
    }

    private fun subscribeUI(){
        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginUiState.collect {
                    // 处于登录状态时 显示退出按钮 和user名
                   if (it.loginState == 0){
                       Log.d(TAG,"user username = ${it.user?.username}")
                       binding.logoutButton.isVisible = true
                       binding.userLoginText.text = it.user?.username
                   }else{
                       // 处于非登录状态时 （例如登录失败或未登录）就不显示 退出按钮
                       binding.logoutButton.isVisible = false
                   }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch(exceptionHandler) {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.logoutUiAction.collect {
                    when (it) {
                        is LogoutUiAction.Success -> {
                            Snackbar.make(
                                binding.root,
                                "Successfully logout",
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding.progressBar.isVisible = false

                            // 退出成功 隐藏 提示点击登录
                            binding.logoutButton.isVisible = false
                            binding.userLoginText.text = getString(R.string.user_login)

                        }
                        is LogoutUiAction.Error -> {
                            Snackbar.make(
                                binding.root,
                                it.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding.progressBar.isVisible = false
                        }
                        is LogoutUiAction.Loading -> {
                            binding.progressBar.isVisible = true
                        }
                    }
                }
            }
        }

    }

}