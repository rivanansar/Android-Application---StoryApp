package com.dicoding.storyapp.data.repos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.storyapp.data.preferences.UserPreference
import com.dicoding.storyapp.data.responses.DetailStoryResponse
import com.dicoding.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class DetailRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    private val _detailStory = MutableLiveData<DetailStoryResponse>()
    val detailStory: LiveData<DetailStoryResponse> = _detailStory

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    suspend fun getDetailStory(id: String) {
        try {
            val userModel = userPreference.getSession().first()
            val token = "Bearer ${userModel.token}"
            val response = withContext(Dispatchers.IO) {
                apiService.getDetailStory(id, token)
            }
            _detailStory.postValue(response)
        } catch (e: Exception) {
            _errorMessage.postValue("Failed to get story details: ${e.message}")
        }
    }

    companion object {
        @Volatile
        private var instance: DetailRepository? = null
        fun getInstance(apiService: ApiService, userPreference: UserPreference): DetailRepository =
            instance ?: synchronized(this) {
                instance ?: DetailRepository(apiService, userPreference)
            }.also { instance = it }
    }
}
