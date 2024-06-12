package com.dicoding.storyapp.ui.adapter

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.dicoding.storyapp.data.local.StoryEntity
import com.dicoding.storyapp.databinding.ItemStoryBinding
import com.dicoding.storyapp.ui.DetailActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class StoryAdapter : PagingDataAdapter<StoryEntity, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    private var clickListener: ((StoryEntity) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
        } else {
            Log.e("StoryAdapter", "Item at position $position is null")
        }
    }

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(story: StoryEntity) {
            binding.apply {
                textName.text = story.name
                textDescription.text = story.description
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .into(imageStory)
                root.setOnClickListener {
                    clickListener?.invoke(story)
                    val context = itemView.context
                    val intent = Intent(context, DetailActivity::class.java)
                    intent.putExtra("storyId", story.id)

                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            Pair(imageStory, "image"),
                            Pair(textName, "username"),
                            Pair(textDescription, "description"),
                        )
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }


    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryEntity>() {
            override fun areItemsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: StoryEntity, newItem: StoryEntity): Boolean {
                return oldItem == newItem
            }
        }
    }
}

