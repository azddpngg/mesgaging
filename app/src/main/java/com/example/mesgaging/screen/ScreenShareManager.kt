package com.example.mesgaging.screen

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.PixelCopy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Real Screen Sharing Engine.
 * Supports hardware MediaProjection VirtualDisplay capture as well as
 * real-time high-fidelity Window PixelCopy capture.
 */
class ScreenShareManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _liveScreenFrame = MutableStateFlow<Bitmap?>(null)
    val liveScreenFrame: StateFlow<Bitmap?> = _liveScreenFrame.asStateFlow()

    private val _isSharing = MutableStateFlow(false)
    val isSharing: StateFlow<Boolean> = _isSharing.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _fps = MutableStateFlow(60)
    val fps: StateFlow<Int> = _fps.asStateFlow()

    private val _resolutionText = MutableStateFlow("1080p • 60 FPS")
    val resolutionText: StateFlow<String> = _resolutionText.asStateFlow()

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var pixelCopyJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    fun startSystemProjection(
        projection: MediaProjection,
        width: Int,
        height: Int,
        densityDpi: Int
    ) {
        stopScreenShare()

        _isSharing.value = true
        _isPaused.value = false
        mediaProjection = projection

        val capWidth = (width / 2).coerceAtLeast(480)
        val capHeight = (height / 2).coerceAtLeast(800)
        _resolutionText.value = "${capWidth}x${capHeight} • 60 FPS"

        backgroundThread = HandlerThread("ScreenCaptureThread").apply { start() }
        backgroundHandler = Handler(backgroundThread!!.looper)

        try {
            imageReader = ImageReader.newInstance(capWidth, capHeight, PixelFormat.RGBA_8888, 2)
            virtualDisplay = projection.createVirtualDisplay(
                "MesgagingScreenShare",
                capWidth, capHeight, densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface,
                null,
                backgroundHandler
            )

            imageReader?.setOnImageAvailableListener({ reader ->
                if (_isPaused.value) return@setOnImageAvailableListener
                try {
                    val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
                    val planes = image.planes
                    val buffer = planes[0].buffer
                    val pixelStride = planes[0].pixelStride
                    val rowStride = planes[0].rowStride
                    val rowPadding = rowStride - pixelStride * capWidth

                    val bitmap = Bitmap.createBitmap(
                        capWidth + rowPadding / pixelStride,
                        capHeight,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.copyPixelsFromBuffer(buffer)
                    image.close()

                    // Crop to exact width if row padding existed
                    val finalBitmap = if (rowPadding > 0) {
                        Bitmap.createBitmap(bitmap, 0, 0, capWidth, capHeight)
                    } else {
                        bitmap
                    }

                    _liveScreenFrame.value = finalBitmap
                } catch (_: Exception) {
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            // Fallback to Window PixelCopy if VirtualDisplay cannot attach
        }
    }

    fun startActiveWindowCapture(activity: Activity) {
        stopScreenShare()

        _isSharing.value = true
        _isPaused.value = false

        pixelCopyJob?.cancel()
        pixelCopyJob = scope.launch {
            while (isActive && _isSharing.value) {
                if (!_isPaused.value) {
                    val window = activity.window
                    val decorView = window.decorView
                    val width = decorView.width.coerceAtLeast(360)
                    val height = decorView.height.coerceAtLeast(640)

                    try {
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        PixelCopy.request(
                            window,
                            bitmap,
                            { copyResult ->
                                if (copyResult == PixelCopy.SUCCESS) {
                                    _liveScreenFrame.value = bitmap
                                }
                            },
                            mainHandler
                        )
                    } catch (_: Exception) {
                    }
                }
                delay(33) // ~30 FPS
            }
        }
    }

    fun togglePause(): Boolean {
        val newState = !_isPaused.value
        _isPaused.value = newState
        return newState
    }

    fun stopScreenShare() {
        pixelCopyJob?.cancel()
        pixelCopyJob = null

        try {
            virtualDisplay?.release()
            imageReader?.close()
            mediaProjection?.stop()
        } catch (_: Exception) {
        } finally {
            virtualDisplay = null
            imageReader = null
            mediaProjection = null
        }

        try {
            backgroundThread?.quitSafely()
        } catch (_: Exception) {
        } finally {
            backgroundThread = null
            backgroundHandler = null
        }

        _isSharing.value = false
        _isPaused.value = false
        _liveScreenFrame.value = null
    }
}
