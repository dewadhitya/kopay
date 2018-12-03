package com.kopi.kematangan.kematangankopi;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity3 extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "MainActivity3";
    JavaCameraView javaCameraView;

    Mat mRgba, imgHsv, saturation_channel, imgObj, rgbObj, invBw;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    javaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        javaCameraView = (JavaCameraView)findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(javaCameraView != null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "openCV loaded!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "openCV not loaded!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgHsv = new Mat(height, width, CvType.CV_8UC3);
        saturation_channel = new Mat();
        imgObj = new Mat();
        invBw = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, imgHsv, Imgproc.COLOR_RGB2HSV);

        //Bagi masing-masing channel : H, S, V
        List<Mat> hsv_channel = new ArrayList<Mat>();

        Core.split(imgHsv,hsv_channel); //Parsing channel HSV

        hsv_channel.get(0); //Hue
        hsv_channel.get(1); //Saturation
        hsv_channel.get(2); //Value

        //Proses ambil objek menggunakan channel Saturation
        saturation_channel = hsv_channel.get(1);
        //Imgproc.threshold(hsv_channel.get(1), imgObj, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        Imgproc.threshold(saturation_channel, imgObj, 128, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        //Inverse Segmentasi
        Imgproc.threshold(imgObj, invBw, 128,255, Imgproc.THRESH_BINARY_INV);

        //Mengambil objek citra hasil segmentasi
        List<Mat> rgb_channel = new ArrayList<Mat>();
        Core.split(mRgba, rgb_channel);

        rgb_channel.get(0); //Red
        rgb_channel.get(1); //Green
        rgb_channel.get(2); //Blue

        Mat r_channel, g_channel, b_channel;
        r_channel = rgb_channel.get(0);
        g_channel = rgb_channel.get(1);
        b_channel = rgb_channel.get(2);

//        Mat r_value = new Mat();
//        Mat g_value = new Mat();
//        Mat b_value = new Mat();
        List<Mat> rgb_value = new ArrayList<>();
        Core.bitwise_and(invBw,rgb_value.get(0),rgb_value.get(0));
        Core.bitwise_and(invBw,rgb_value.get(1),rgb_value.get(1));
        Core.bitwise_and(invBw,rgb_value.get(2),rgb_value.get(2));

        rgbObj = new Mat();
        Core.merge(rgb_value,rgbObj);
//        for (int i=0; i<r_channel.height(); i++){
//            for (int j=0; j<r_channel.width(); j++){
//
//            }
//        }
        return rgbObj;
    }
}
