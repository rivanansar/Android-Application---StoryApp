package com.dicoding.storyapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import java.io.File
import com.dicoding.storyapp.data.repos.UploadRepository

class UploadStoryViewModel(private val repository: UploadRepository) : ViewModel() {
    fun uploadImage(token: String, file: File, description: String, lat: Double?, lon: Double?) =
        repository.uploadImage(token, file, description, lat, lon)
}
