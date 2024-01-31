package com.example.portfolio

import android.app.Activity.RESULT_OK
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class ScreenCaptureService : Service() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var imageReader: ImageReader
    private var virtualDisplay: VirtualDisplay? = null

    private var isCapturing = false

    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val SCREEN_CAPTURE_REQUEST_CODE = 1001
    }

    override fun onCreate() {
        super.onCreate()
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        startScreenCapture()

    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
//            data?.let {
//                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, it)
//                startVirtualDisplay()
//            }
//        }
//    }

    fun startScreenCapture() {
//        val startIntent = mediaProjectionManager.createScreenCaptureIntent()

        val startIntent = Intent(this, ScreenCaptureService::class.java)
        ContextCompat.startForegroundService(this, startIntent) // Use ContextCompat for compatibility

    }

//    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
////        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == SCREEN_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
//            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
//            startVirtualDisplay()
//        }
//    }

    private fun startVirtualDisplay() {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        imageReader = ImageReader.newInstance(
            screenWidth,
            screenHeight,
            PixelFormat.RGBA_8888,
            2
        )

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            screenWidth,
            screenHeight,
            displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader.surface,
            null,
            null
        )

        isCapturing = true

        // Schedule a runnable to capture the screen periodically
        handler.postDelayed({
            captureScreen()
        }, 100) // Adjust the delay as needed
    }

    private fun captureScreen() {
        if (isCapturing) {
            val image = imageReader.acquireLatestImage()
            if (image != null) {
                val buffer: ByteBuffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                saveBitmap(bytes)

                image.close()
            }

            // Schedule the next capture
            handler.postDelayed({
                captureScreen()
            }, 100) // Adjust the delay as needed
        }
    }

    private fun saveBitmap(bytes: ByteArray) {
        try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Screenshot_$timestamp.png"

            val file = File(externalMediaDirs.first(), fileName) // Get external storage directory
            val fileOutputStream = FileOutputStream(file)
            val bitmap =
                Bitmap.createBitmap(imageReader.width, imageReader.height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes))
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.close()

            // Handle screenshot capture success (optional)
            // ...
        } catch (e: IOException) {
            // Handle screenshot capture failure (optional)
            // ...
        }
    }

    private fun Service.onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ScreenCaptureService.SCREEN_CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, it) // Use safe call
                startVirtualDisplay()
            }
        }
    }


}


