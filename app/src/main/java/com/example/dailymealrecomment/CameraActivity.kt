package com.example.dailymealrecomment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.dailymealrecomment.databinding.ActivityCameraBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            showCameraState()
            startCamera()
        } else {
            showPermissionDeniedState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.imageCaptureButton.setOnClickListener { takePhoto() }
        binding.btnRetryCameraPermission.setOnClickListener { requestCameraAccess() }
        binding.btnOpenCameraSettings.setOnClickListener { openAppSettings() }

        if (intent.getBooleanExtra(EXTRA_FORCE_PERMISSION_DENIED, false)) {
            showPermissionDeniedState()
            return
        }

        requestCameraAccess()
    }

    private fun requestCameraAccess() {
        if (hasCameraPermission()) {
            showCameraState()
            startCamera()
        } else {
            showPermissionRequestState()
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        setStatus(getString(R.string.camera_starting), showProgress = true)
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            runCatching {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = binding.viewFinder.surfaceProvider
                }
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                cameraProvider = provider
                provider.unbindAll()
                provider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
                setStatus(getString(R.string.camera_ready), showProgress = false)
            }.onFailure {
                showCameraErrorState()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: run {
            setStatus(getString(R.string.camera_not_ready), showProgress = false)
            return
        }
        val photoFile = File.createTempFile("meal_", ".jpg", cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        setCapturingState(true)
        capture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread {
                        startActivity(Intent(this@CameraActivity, FoodAnalysisActivity::class.java).apply {
                            putExtra(FoodAnalysisActivity.EXTRA_IMAGE_URI, Uri.fromFile(photoFile).toString())
                        })
                        finish()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread {
                        setCapturingState(false)
                        setStatus(getString(R.string.photo_capture_failed), showProgress = false)
                        Toast.makeText(this@CameraActivity, R.string.photo_capture_failed, Toast.LENGTH_LONG).show()
                    }
                }
            },
        )
    }

    private fun showCameraState() {
        binding.viewFinder.isVisible = true
        binding.permissionState.isVisible = false
        binding.imageCaptureButton.isEnabled = true
        binding.imageCaptureButton.text = getString(R.string.camera_capture)
        binding.cameraStatusPanel.isVisible = true
        binding.cameraProgress.isVisible = false
        binding.tvCameraStatus.text = getString(R.string.camera_ready)
    }

    private fun showPermissionRequestState() {
        binding.viewFinder.isVisible = false
        binding.permissionState.isVisible = true
        binding.imageCaptureButton.isEnabled = false
        binding.tvPermissionTitle.text = getString(R.string.camera_permission_title)
        binding.tvPermissionMessage.text = getString(R.string.camera_permission_message)
        binding.btnRetryCameraPermission.text = getString(R.string.camera_permission_button)
        binding.btnOpenCameraSettings.visibility = View.GONE
        binding.cameraStatusPanel.isVisible = false
    }

    private fun showPermissionDeniedState() {
        binding.viewFinder.isVisible = false
        binding.permissionState.isVisible = true
        binding.imageCaptureButton.isEnabled = false
        binding.tvPermissionTitle.text = getString(R.string.camera_permission_denied_title)
        binding.tvPermissionMessage.text = getString(R.string.camera_permission_denied_message)
        binding.btnRetryCameraPermission.text = getString(R.string.camera_permission_button)
        binding.btnOpenCameraSettings.visibility = View.VISIBLE
        binding.cameraStatusPanel.isVisible = false
    }

    private fun showCameraErrorState() {
        binding.viewFinder.isVisible = false
        binding.permissionState.isVisible = true
        binding.imageCaptureButton.isEnabled = false
        binding.tvPermissionTitle.text = getString(R.string.camera_error_title)
        binding.tvPermissionMessage.text = getString(R.string.camera_start_failed)
        binding.btnRetryCameraPermission.text = getString(R.string.camera_retry)
        binding.btnOpenCameraSettings.visibility = View.GONE
        binding.cameraStatusPanel.isVisible = false
    }

    private fun setCapturingState(isCapturing: Boolean) {
        binding.imageCaptureButton.isEnabled = !isCapturing
        binding.imageCaptureButton.text = getString(
            if (isCapturing) R.string.camera_capturing else R.string.camera_capture,
        )
        setStatus(
            getString(if (isCapturing) R.string.camera_saving_photo else R.string.camera_ready),
            showProgress = isCapturing,
        )
    }

    private fun setStatus(message: String, showProgress: Boolean) {
        binding.cameraStatusPanel.isVisible = true
        binding.cameraProgress.isVisible = showProgress
        binding.tvCameraStatus.text = message
    }

    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null),
        )
        startActivity(intent)
    }

    override fun onDestroy() {
        cameraProvider?.unbindAll()
        cameraExecutor.shutdown()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_FORCE_PERMISSION_DENIED = "extra_force_permission_denied"
    }
}
