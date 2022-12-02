package com.example.jetpackdemo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.ProjectContent
import com.example.jetpackdemo.bean.ProjectTree
import com.example.jetpackdemo.repository.WanAndroidRepository
import com.example.jetpackdemo.util.ExceptionHandler.exceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel(){
    private val _projectTreeListStateFlow = MutableStateFlow<List<ProjectTree>>(listOf())

    val projectTreeListStateFlow: StateFlow<List<ProjectTree>> = _projectTreeListStateFlow

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

    fun loadProjectContentById(id: Int): Flow<PagingData<ProjectContent>> {
        return  repository.loadContentById(id).cachedIn(viewModelScope)
    }

}