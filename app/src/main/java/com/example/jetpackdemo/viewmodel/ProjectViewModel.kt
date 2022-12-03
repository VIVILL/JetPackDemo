package com.example.jetpackdemo.viewmodel

import android.util.Log
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

private const val TAG = "ProjectViewModel"

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val repository: WanAndroidRepository
) : ViewModel(){
    private val _projectTreeListStateFlow = MutableStateFlow<List<ProjectTree>>(listOf())
    val projectTreeListStateFlow: StateFlow<List<ProjectTree>> = _projectTreeListStateFlow

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

    private var projectId = 0

    lateinit var projectContentFlow: Flow<PagingData<ProjectContent>>

    fun intiProjectContentFlow(id: Int){
        Log.d(TAG,"inner intiProjectContentFlow projectId = $projectId id = $id")
        projectId = id
        loadProjectContent()
    }

    fun loadProjectContent(){
        Log.d(TAG,"inner loadProjectContentById projectId = $projectId")
        projectContentFlow =   repository.loadContentById(projectId).cachedIn(viewModelScope)
    }

}