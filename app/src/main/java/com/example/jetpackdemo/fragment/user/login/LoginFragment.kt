package com.example.jetpackdemo.fragment.user.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jetpackdemo.databinding.FragmentLoginBinding
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import com.example.jetpackdemo.viewmodel.LoginUiAction
import com.example.jetpackdemo.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


private const val TAG = "LoginFragment"

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        // 设置toolbar
        binding.toolbar.title = "登录"
        binding.toolbar.setNavigationOnClickListener { view ->
            findNavController().navigateUp()
        }

        // 把 存储的 用户账号密码 放到输入账号的输入框中
        if (viewModel.getRememberAccount()){
            binding.etUserName.setText("${viewModel.getLocalName()}")
            binding.etPassword.setText("${viewModel.getLocalPassword()}")
        }

        binding.btnLogin.setOnClickListener {
            val userName = binding.etUserName.text.toString().trim(' ') //去掉空格
            val password = binding.etPassword.text.toString().trim(' ') //去掉空格

            if (viewModel.getRememberAccount()){
                viewModel.setUserNameSP(userName) //保存用户名
                viewModel.setUserPasswordSP(password) //保存密码
            }else{
                // 清除 sp 保存的 name 和 password
                viewModel.setUserNameSP("")
                viewModel.setUserPasswordSP("")
            }

            viewModel.login(userName,password)
        }

        if (viewModel.getRememberAccount() && viewModel.getLocalName() != "" && viewModel.getLocalPassword() != ""){
            binding.checkBoxAccount.isChecked = true
        }
        if (viewModel.getAutoLogin()){
            binding.checkBoxAutoLogin.isChecked = true
        }

        binding.checkBoxAccount.setOnCheckedChangeListener(object:  CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                Log.d(TAG,"inner checkBoxAccount onCheckedChanged isChecked = $isChecked")
                //记住密码选框发生改变时
                if(isChecked){
                   // 设置  rememberAccount 为 true
                    viewModel.setRememberAccountSP(true)
                }else{
                    viewModel.setRememberAccountSP(false)
                }

            }

        })

        binding.checkBoxAutoLogin.setOnCheckedChangeListener(object:  CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                Log.d(TAG,"inner checkBoxAutoLogin onCheckedChanged isChecked = $isChecked")
                //记住密码选框发生改变时
                if(isChecked){
                    // 设置  autoLogin 为 true
                    viewModel.setAutoLoginSP(true)
                }else{
                    viewModel.setAutoLoginSP(false)
                }

            }

        })

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
                viewModel.loginUiAction.collect {
                    when (it) {
                        is LoginUiAction.Success -> {
                            binding.progressBar.isVisible = false
                            // 登录成功后回退上一个 fragment
                            findNavController().navigateUp()
                        }
                        is LoginUiAction.Error -> {
                            Snackbar.make(
                                binding.root,
                                it.message,
                                Snackbar.LENGTH_SHORT
                            ).show()
                            binding.progressBar.isVisible = false
                        }
                        is LoginUiAction.Loading -> {
                            binding.progressBar.isVisible = true
                        }
                    }
                }
            }
        }

    }

}