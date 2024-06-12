package com.dicoding.storyapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.responses.StoryResponse
import com.dicoding.storyapp.data.repos.UserRepository
import com.dicoding.storyapp.data.preferences.UserModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MapsViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _storyWithLocation = MutableStateFlow<StoryResponse?>(null)
    val storyWithLocation: StateFlow<StoryResponse?> get() = _storyWithLocation

    fun getStoriesWithLocation(token: String) {
        viewModelScope.launch {
            try {
                val response = userRepository.getStoriesWithLocation(token)
                _storyWithLocation.value = response
                Log.d("MapsViewModel", "Stories with location: $response")
            } catch (e: HttpException) {
                Log.e("MapsViewModel", "HTTP error: ${e.response()?.errorBody()?.string()}", e)
                _storyWithLocation.value = null
            } catch (e: Exception) {
                Log.e("MapsViewModel", "Error fetching stories", e)
                _storyWithLocation.value = null
            }
        }
    }

    fun getSession(): Flow<UserModel> {
        return userRepository.getSession()
    }
}
