package com.example.jetpackdemo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.jetpackdemo.bean.ProjectContent
import com.example.jetpackdemo.repository.WanAndroidRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ProjectDetailViewModel"

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val repository: WanAndroidRepository,
    private val projectId: Int
) : ViewModel(){

    val loadProjectAction: (UiAction) -> Unit

    private val _actionSharedFlow = MutableSharedFlow<UiAction>()
    val projectContent: Flow<PagingData<ProjectContent>>

    init {
        Log.d(TAG, "inner init")
        val actionStateFlow = _actionSharedFlow
            .filterIsInstance<UiAction.LoadProject>()
            .distinctUntilChanged()
            .onStart {
                Log.d(TAG, "before emit LoadProject,projectId = $projectId")
                emit(UiAction.LoadProject(id = projectId))
            }
        Log.d(TAG, "before init projectContent")
        projectContent = actionStateFlow.flatMapLatest {value ->
            Log.d(TAG, "value id = ${value.id}")
            loadContentById(projectId = value.id)
        }.cachedIn(viewModelScope)
        loadProjectAction = { action ->
            viewModelScope.launch {
                _actionSharedFlow.emit(action)
            }
        }
    }

    private fun loadContentById(projectId: Int): Flow<PagingData<ProjectContent>>{
        return repository.loadContentById(projectId)
    }


}

sealed class UiAction {
    data class LoadProject(val id: Int) : UiAction()
}