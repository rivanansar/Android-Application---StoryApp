package com.dicoding.storyapp.data.repos

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.storyapp.data.StoryRemoteMediator
import com.dicoding.storyapp.data.local.StoryDatabase
import com.dicoding.storyapp.data.local.StoryEntity
import com.dicoding.storyapp.data.preferences.UserModel
import com.dicoding.storyapp.data.preferences.UserPreference
import com.dicoding.storyapp.data.responses.StoryResponse
import com.dicoding.storyapp.data.retrofit.ApiConfig
import com.dicoding.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun saveToken(token: String) {
        userPreference.saveToken(token)
    }

    suspend fun getStoriesWithLocation(token: String): StoryResponse {
        val formattedToken = "Bearer $token"
        return ApiConfig.getApiService().getStoriesWithLocation(formattedToken, 1)
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getStories(token: String): LiveData<PagingData<StoryEntity>> {
        val pagingSourceFactory = { storyDatabase.storyDao().getAllStories() }
        //val formattedToken = "Bearer $token"
        Log.d("UserRepository", "Token for Paging: $token")

        return Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = StoryRemoteMediator(apiService, storyDatabase, token),
            pagingSourceFactory = pagingSourceFactory
        ).liveData
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService,
            storyDatabase: StoryDatabase
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService, storyDatabase)
            }.also { instance = it }
    }
}
