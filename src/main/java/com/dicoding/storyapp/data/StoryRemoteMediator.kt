package com.dicoding.storyapp.data

import android.util.Log
import androidx.paging.*
import androidx.room.withTransaction
import com.dicoding.storyapp.data.local.RemoteKeys
import com.dicoding.storyapp.data.local.StoryDatabase
import com.dicoding.storyapp.data.local.StoryEntity
import com.dicoding.storyapp.data.retrofit.ApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
    private val token: String
) : RemoteMediator<Int, StoryEntity>() {

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, StoryEntity>
    ): MediatorResult {
        val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
        Log.d("RemoteMediator", "Using token: $formattedToken")
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        return try {
            val response = apiService.getStories(formattedToken, page, state.config.pageSize)
            val stories = response.listStory.map {
                StoryEntity(
                    id = it.id ?: "",
                    name = it.name ?: "",
                    description = it.description ?: "",
                    photoUrl = it.photoUrl ?: "",
                    lon = it.lon,
                    lat = it.lat
                )
            }

            val endOfPaginationReached = stories.isEmpty()

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteKeysDao().deleteRemoteKeys()
                    storyDatabase.storyDao().deleteAllStories()
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = stories.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                storyDatabase.remoteKeysDao().insertAll(keys)
                storyDatabase.storyDao().insertStories(stories)
            }
            Log.d("RemoteMediator", "Loaded page $page with ${stories.size} stories")
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: IOException) {
            Log.e("RemoteMediator", "IOException: ${exception.message}")
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            Log.e("RemoteMediator", "HttpException: ${exception.message}")
            MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { story ->
            storyDatabase.remoteKeysDao().getRemoteKeysId(story.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { story ->
            storyDatabase.remoteKeysDao().getRemoteKeysId(story.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, StoryEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                storyDatabase.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }
}
