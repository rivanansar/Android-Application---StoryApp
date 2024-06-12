package com.dicoding.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.responses.LoginResponse
import com.dicoding.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.dicoding.storyapp.data.repos.UserRepository
import com.dicoding.storyapp.data.preferences.UserModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _loginResult = MutableLiveData<LoginResponse?>()
    val loginResult: LiveData<LoginResponse?> = _loginResult

    fun login(email: String, password: String) {
        val client = ApiConfig.getApiService().login(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    _loginResult.value = response.body()
                    response.body()?.loginResult?.token?.let { saveTokenToDataStore(it) } // Save token to DataStore
                } else {
                    _loginResult.value = null
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _loginResult.value = null
            }
        })
    }

    private fun saveTokenToDataStore(token: String) {
        viewModelScope.launch {
            repository.saveToken(token)
        }
    }
    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }
}