package com.dicoding.storyapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.responses.RegisterResponse
import com.dicoding.storyapp.data.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableLiveData<RegisterResponse?>()
    val registerResult: LiveData<RegisterResponse?> = _registerResult

    fun register(name: String, email: String, password: String) {
        val client = ApiConfig.getApiService().register(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    _registerResult.value = response.body()
                } else {
                    val errorMessage = when (response.code()) {
                        409 -> "Email sudah dipakai, silahkan gunakan email lain"
                        else -> "Gagal melakukan registrasi"
                    }
                    _registerResult.value = RegisterResponse(error = true, message = errorMessage)
                }
            }
            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _registerResult.value = null
            }
        })
    }
}