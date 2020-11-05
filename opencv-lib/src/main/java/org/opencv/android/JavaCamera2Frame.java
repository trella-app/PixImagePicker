package org.opencv.android;

import android.graphics.ImageFormat;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class JavaCamera2Frame implements CameraBridgeViewBase.CvCameraViewFrame {
    @Override
    public Mat gray() {
        return mYuvFrameData.submat(0, mHeight, 0, mWidth);
    }

    @Override
    public Mat rgba() {
        if (mPreviewFormat == ImageFormat.NV21)
            Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
        else if (mPreviewFormat == ImageFormat.YV12)
            Imgproc.cvtColor(mYuvFrameData, mRgba, Imgproc.COLOR_YUV2RGB_I420, 4); // COLOR_YUV2RGBA_YV12 produces inverted colors
        else if (mPreviewFormat == ImageFormat.YUV_420_888) {
            assert (mUVFrameData != null);
            Imgproc.cvtColorTwoPlane(mYuvFrameData, mUVFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21);
        } else
            throw new IllegalArgumentException("Preview Format can be NV21 or YV12");

        return mRgba;
    }

    public JavaCamera2Frame(Mat Yuv420sp, int width, int height) {
        super();
        mWidth = width;
        mHeight = height;
        mYuvFrameData = Yuv420sp;
        mUVFrameData = null;
        mRgba = new Mat();
    }

    public JavaCamera2Frame(Mat Y, Mat UV, int width, int height) {
        super();
        mWidth = width;
        mHeight = height;
        mYuvFrameData = Y;
        mUVFrameData = UV;
        mRgba = new Mat();
    }

    public void release() {
        mRgba.release();
    }

    private Mat mYuvFrameData;
    private Mat mUVFrameData;
    private Mat mRgba;
    private int mWidth;
    private int mHeight;
}