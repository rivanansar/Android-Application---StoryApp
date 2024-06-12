package com.dicoding.storyapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.repos.*
import com.dicoding.storyapp.data.preferences.dataStore
import com.dicoding.storyapp.data.preferences.UserPreference


class ViewModelFactory(
    private val userRepository: UserRepository,
    private val uploadRepository: UploadRepository,
    private val detailRepository: DetailRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(userRepository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(detailRepository) as T
            }
            modelClass.isAssignableFrom(UploadStoryViewModel::class.java) -> {
                UploadStoryViewModel(uploadRepository) as T
            }
            modelClass.isAssignableFrom(MapsViewModel::class.java) ->{
                MapsViewModel(userRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        @JvmStatic
        fun getInstance(context: Context): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    val userPreference = UserPreference.getInstance(context.dataStore)
                    INSTANCE = ViewModelFactory(
                        Injection.provideUserRepository(context),
                        Injection.provideUploadRepository(),
                        Injection.provideDetailRepository(userPreference)
                    )
                }
            }
            return INSTANCE as ViewModelFactory
        }
    }
}