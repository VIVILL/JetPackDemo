package com.example.jetpackdemo.viewmodel

import android.util.Log
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.ProjectContent
import com.example.jetpackdemo.bean.ProjectTree
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

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
