package com.example.jetpackdemo.di

import android.util.Log
import com.example.jetpackdemo.viewmodel.ProjectViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class ViewModelModule {

    @Provides
    fun provideProjectId(): Int {
        Log.d("ViewModelModule","provideProjectId")
        return ProjectViewModel.projectId
    }
}