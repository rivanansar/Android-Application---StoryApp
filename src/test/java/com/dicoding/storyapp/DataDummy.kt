package com.dicoding.storyapp

import com.dicoding.storyapp.data.local.StoryEntity

object DataDummy {

    fun generateDummyStoryResponse(): List<StoryEntity> {
        val items: MutableList<StoryEntity> = arrayListOf()
        for (i in 0..100) {
            val story = StoryEntity(
                id = i.toString(),
                name = "name $i",
                description = "description $i",
                photoUrl = "photoUrl $i",
                lon = i.toDouble(),
                lat = i.toDouble()
            )
            items.add(story)
        }
        return items
    }
}
