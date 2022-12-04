package com.example.jetpackdemo.fragment.web

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.navigation.fragment.findNavController
import com.example.jetpackdemo.databinding.FragmentWebBinding
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "WebFragment"

@AndroidEntryPoint
class WebFragment : Fragment() {
    private var _binding: FragmentWebBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWebBinding.inflate(inflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener { view ->
            findNavController().navigateUp()
        }

        binding.toolbar.title = arguments?.getString("title")
        configureWebView(arguments?.getString("link") ?: "")
    }

    override fun onDestroyView() {
        Log.d(TAG,"inner onDestroyView")
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView(link: String) {
        WebView.setWebContentsDebuggingEnabled(true)
        // Enable Javascript
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.loadUrl(link)

    }

}