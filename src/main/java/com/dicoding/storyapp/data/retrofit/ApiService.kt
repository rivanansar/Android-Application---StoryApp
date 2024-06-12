package com.dicoding.storyapp.data.retrofit

import com.dicoding.storyapp.data.responses.DetailStoryResponse
import com.dicoding.storyapp.data.responses.FileUploadResponse
import com.dicoding.storyapp.data.responses.LoginResponse
import com.dicoding.storyapp.data.responses.RegisterResponse
import com.dicoding.storyapp.data.responses.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @GET("stories/{id}")
    suspend fun getDetailStory(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): DetailStoryResponse

        @Multipart
        @POST("stories")
        suspend fun uploadImage(
            @Header("Authorization") token: String,
            @Part file: MultipartBody.Part,
            @Part("description") description: RequestBody,
            @Part("lat") lat: RequestBody?,
            @Part("lon") lon: RequestBody?
        ): FileUploadResponse

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1
    ): StoryResponse

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoryResponse
}
