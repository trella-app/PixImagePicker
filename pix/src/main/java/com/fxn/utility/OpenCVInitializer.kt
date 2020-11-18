package com.fxn.utility

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import org.opencv.android.InstallCallbackInterface
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.android.OpenCVLoader.initAsync

class OpenCVInitializer :ContentProvider(){
    private val TAG = "openCVLoader"
    override fun onCreate(): Boolean {
        initOpenCV()
        return true
    }
    private fun initOpenCV() {
        val wasEngineInitialized = OpenCVLoader.initDebug()
        if (wasEngineInitialized){
            Log.d(TAG, "The OpenCV was successfully initialized in debug mode using .so libs.")
        } else {
            initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, context, object : LoaderCallbackInterface {
                override fun onManagerConnected(status: Int) {
                    when(status) {
                        LoaderCallbackInterface.SUCCESS -> Log.d(TAG,"OpenCV successfully started.")
                        LoaderCallbackInterface.INIT_FAILED -> Log.d(TAG,"Failed to start OpenCV.")
                        LoaderCallbackInterface.MARKET_ERROR -> Log.d(TAG,"Google Play Store could not be invoked. Please check if you have the Google Play Store app installed and try again.")
                        LoaderCallbackInterface.INSTALL_CANCELED -> Log.d(TAG,"OpenCV installation has been cancelled by the user.")
                        LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION -> Log.d(TAG,"This version of OpenCV Manager is incompatible. Possibly, a service update is required.")
                    }
                }

                override fun onPackageInstall(operation: Int, callback: InstallCallbackInterface?) {
                    Log.d(TAG,"OpenCV Manager successfully installed from Google Play.")
                }
            })
        }
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return -1110
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
      return -1110
    }

}