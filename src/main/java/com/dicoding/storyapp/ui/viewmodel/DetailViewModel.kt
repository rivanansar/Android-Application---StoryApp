package com.dicoding.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.dicoding.storyapp.data.repos.DetailRepository
import com.dicoding.storyapp.data.responses.DetailStoryResponse

class DetailViewModel(private val detailRepository: DetailRepository) : ViewModel() {
    val detailStory: LiveData<DetailStoryResponse> = detailRepository.detailStory
    val errorMessage: LiveData<String> = detailRepository.errorMessage

    fun getDetailStory(id: String) {
        viewModelScope.launch {
            detailRepository.getDetailStory(id)
        }
    }
}
