package com.fxn.utility

import android.graphics.ImageFormat
import android.media.Image
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.otaliastudios.cameraview.CameraView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.JavaCamera2Frame
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfDouble
import org.opencv.imgproc.Imgproc
import kotlin.math.pow


class BlueImageDetection(activity: AppCompatActivity) {
    private val TAG = "openCVLoader"
    private var isLoaded = false
    private val mPreviewFormat = ImageFormat.YUV_420_888

    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(activity) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    isLoaded = true
                }
                else -> {
                    isLoaded = false
                    super.onManagerConnected(status)
                }
            }
        }
    }

    init {
        activity.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                if (!OpenCVLoader.initDebug()) {
                    Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
                    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, activity, mLoaderCallback)
                } else {
                    Log.d(TAG, "OpenCV library found inside package. Using it!")
                    mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
                }
            }

        })
    }


    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun startDetection(camera: CameraView, onBlurValueCallback: OnBlurDetectionCallback) {

        camera.addFrameProcessor { frame ->
            if (isLoaded) {
                if (frame.getData<Any>() is Image) {
                    runBlocking {
                        withContext(Dispatchers.IO) {
                            val image = frame.getData<Image>()
                            val previewSize = camera.pictureSize
                            previewSize?.let {
                                val w = it.width
                                val h = it.height
                                val planes = image.planes
                                val y_plane = planes[0].buffer
                                val uv_plane = planes[1].buffer
                                val y_mat = Mat(h, w, CvType.CV_8UC1, y_plane)
                                val uv_mat = Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane)
                                val frame = JavaCamera2Frame(y_mat, uv_mat, w, h)
                                val std = blurLevel1(frame.gray())
                                withContext(Dispatchers.Main) {
                                    onBlurValueCallback.onValue(std)
                                }
//                    startBlurDetection(blurLevel2(it.gray()))
//                    startBlurDetection(blurLevel3(it.gray()))
                            }
                        }
                    }


                }
            }

        }
    }

    //base algorithm
    fun blurLevel1(matGray: Mat): Double {
        val destination = Mat()
        Imgproc.Laplacian(matGray, destination, 3)
        val median = MatOfDouble()
        val std = MatOfDouble()
        Core.meanStdDev(destination, median, std)
        return std.get(0, 0)[0].pow(2.0)

    }

    fun blurLevel2(matGray: Mat): Double {
        val destination = Mat()
        val kernel = object : Mat(3, 3, CvType.CV_32F) {
            init {
                put(0, 0, 0.0)
                put(0, 1, -1.0)
                put(0, 2, 0.0)

                put(1, 0, -1.0)
                put(1, 1, 4.0)
                put(1, 2, -1.0)

                put(2, 0, 0.0)
                put(2, 1, -1.0)
                put(2, 2, 0.0)
            }
        }
        Imgproc.filter2D(matGray, destination, -1, kernel)
        val median = MatOfDouble()
        val std = MatOfDouble()
        Core.meanStdDev(destination, median, std)

        return Math.pow(std.get(0, 0)[0], 2.0)
    }

    //standard deviation based algorithm
    fun blurLevel3(image: Mat): Double {
        val mu = MatOfDouble() // mean
        val sigma = MatOfDouble() // standard deviation
        Core.meanStdDev(image, mu, sigma)
        return Math.pow(mu.get(0, 0)[0], 2.0)
    }


}

interface OnBlurDetectionCallback {
    fun onValue(double: Double)
}