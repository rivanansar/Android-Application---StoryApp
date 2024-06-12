package com.dicoding.storyapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val lon: Double?,
    val lat: Double?
)
