package com.dicoding.storyapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityDetailBinding
import com.dicoding.storyapp.ui.viewmodel.DetailViewModel
import com.dicoding.storyapp.ui.viewmodel.ViewModelFactory
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailstory)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.title = getString(R.string.detail_story)

        val storyId = intent.getStringExtra("storyId")
        if (storyId != null) {
            detailViewModel.getDetailStory(storyId)
        } else {
            Toast.makeText(this, "Story ID is missing", Toast.LENGTH_SHORT).show()
        }

        detailViewModel.detailStory.observe(this) { response ->
            if (response != null) {
                binding.textName.text = response.story?.name
                binding.textDescription.text = response.story?.description
                Glide.with(this)
                    .load(response.story?.photoUrl)
                    .into(binding.imageStory)
            }
        }

        detailViewModel.errorMessage.observe(this) { errorMessage ->
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
