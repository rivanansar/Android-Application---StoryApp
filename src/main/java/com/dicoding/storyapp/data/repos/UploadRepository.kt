package com.dicoding.storyapp.data.repos

import androidx.lifecycle.liveData
import com.dicoding.storyapp.data.responses.FileUploadResponse
import com.dicoding.storyapp.data.retrofit.ApiService
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class UploadRepository private constructor(private val apiService: ApiService) {
    fun uploadImage(token: String, imageFile: File, description: String, lat: Double?, lon: Double?) = liveData {
        emit(ResultState.Loading)
        val requestBody = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)

        val latPart = lat?.toString()?.toRequestBody("text/plain".toMediaType())
        val lonPart = lon?.toString()?.toRequestBody("text/plain".toMediaType())

        try {
            val successResponse = apiService.uploadImage("Bearer $token", multipartBody, requestBody, latPart, lonPart)
            emit(ResultState.Success(successResponse))
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
            emit(ResultState.Error(errorResponse.message))
        }
    }

    companion object {
        @Volatile
        private var instance: UploadRepository? = null
        fun getInstance(apiService: ApiService) = instance ?: synchronized(this) { instance ?: UploadRepository(apiService) }.also { instance = it }
    }
}

