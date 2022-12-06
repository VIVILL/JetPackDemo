package com.example.jetpackdemo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpackdemo.bean.ProjectTree
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProjectViewModel"

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel(){
    private val _projectTreeListStateFlow = MutableStateFlow<List<ProjectTree>>(listOf())
    val projectTreeListStateFlow: StateFlow<List<ProjectTree>> = _projectTreeListStateFlow

    companion object {
        var projectId = 0
    }

    fun setStartProjectId(id: Int){
        Log.d(TAG,"inner setStartProjectId")
        projectId = id
    }

    init {
        loadProjectTreeList()
    }

    fun loadProjectTreeList() {
        // 更新  value 数据
        viewModelScope.launch(exceptionHandler) {
            _projectTreeListStateFlow.value = repository.loadProjectTree()
                .catch { throwable ->
                    println(throwable)
                }
                .stateIn(viewModelScope)
                .value
        }
    }
}
