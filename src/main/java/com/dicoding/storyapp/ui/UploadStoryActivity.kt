package com.dicoding.storyapp.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.data.repos.ResultState
import com.dicoding.storyapp.data.preferences.UserPreference
import com.dicoding.storyapp.data.preferences.dataStore
import com.dicoding.storyapp.databinding.ActivityUploadStoryBinding
import com.dicoding.storyapp.ui.utilization.LocationUtil
import com.dicoding.storyapp.ui.utilization.getImageUri
import com.dicoding.storyapp.ui.utilization.reduceFileImage
import com.dicoding.storyapp.ui.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.dicoding.storyapp.ui.utilization.uriToFile
import com.dicoding.storyapp.ui.viewmodel.UploadStoryViewModel


class UploadStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadStoryBinding
    private var currentImageUri: Uri? = null
    private var locationEnabled: Boolean = false
    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private val viewModel by viewModels<UploadStoryViewModel> { ViewModelFactory.getInstance(applicationContext) }
    private lateinit var locationUtil: LocationUtil

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationUtil = LocationUtil(this)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        binding.locationSwitch.setOnCheckedChangeListener { _, isChecked ->
            locationEnabled = isChecked
            if (isChecked) {
                getCurrentLocation()
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCameraWithPermissionCheck() }
        binding.uploadButton.setOnClickListener { uploadImage() }
    }

    private fun getCurrentLocation() {
        locationUtil.getCurrentLocation({ location ->
            currentLatitude = location.latitude
            currentLongitude = location.longitude
            Log.d("UploadStoryActivity", "Current location: $location")
        }, requestPermissionLauncher)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCameraWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun showConfirmationDialog(onConfirm: () -> Unit) {
        AlertDialog.Builder(this).apply {
            setTitle("Konfirmasi Upload")
            setMessage("Apakah Anda yakin ingin mengunggah gambar ini?")
            setPositiveButton("Ya") { _, _ -> onConfirm() }
            setNegativeButton("Tidak") { dialog, _ -> dialog.dismiss() }
            create()
            show()
        }
    }

    private fun uploadImage() {
        val imageUri = currentImageUri ?: return
        val file = uriToFile(imageUri, this).reduceFileImage()
        val description = binding.descriptionEditText.text.toString()
        val userPreference = UserPreference.getInstance(applicationContext.dataStore)

        showConfirmationDialog {
            runBlocking {
                val userModel = userPreference.getSession().first()
                if (userModel.isLogin) {
                    val lat = if (locationEnabled) currentLatitude else null
                    val lon = if (locationEnabled) currentLongitude else null
                    viewModel.uploadImage(userModel.token, file, description, lat, lon).observe(this@UploadStoryActivity) { result ->
                        when (result) {
                            is ResultState.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is ResultState.Success -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this@UploadStoryActivity, result.data.message, Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            is ResultState.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this@UploadStoryActivity, result.error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@UploadStoryActivity, "User not logged in", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
