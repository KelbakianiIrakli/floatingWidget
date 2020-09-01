package com.jwhh.floatingapp;



import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.jwhh.floatingapp.CameraPreview.mCamera;

public class CameraService extends Service {

    String TAG = "log message";
    static String format_type = "jpg";

    WindowManager windowManager;
    WindowManager.LayoutParams rootParams, buttonParams;
    RelativeLayout rootLayout;
    ImageView captureButton, closeButton;


    Camera camera = null;
    Camera.Parameters cameraParams;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;

    int xMargin = 0;
    int yMargin = 0;
    int statusBarHeight = 0;
    private boolean isRecording = false;
    boolean dragFlag = false;


    SharedPreferences preferences;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return Service.START_STICKY;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File error: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Not access rights: " + e.getMessage());
            }
        }
    };
    @Override
    public void onCreate() {

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        statusBarHeight = (int) (24 * getResources().getDisplayMetrics().density);

        rootLayout = new RelativeLayout(this);
        rootParams = new WindowManager.LayoutParams(300, 400, WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        rootParams.gravity = Gravity.TOP | Gravity.START;

        camera = Camera.open();
        applyCameraSetting();

        mPreview = new CameraPreview(this, camera);
        rootLayout.addView(mPreview);
        addButtons();
        windowManager.addView(rootLayout, rootParams);
        addDragFunction();

    }


    void applyCameraSetting() {
        cameraParams = camera.getParameters();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        }
                cameraParams.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                cameraParams.setColorEffect(Camera.Parameters.EFFECT_NONE);
        camera.setParameters(cameraParams);
        }


        //  camera picture size setting




    //=========     layout functions       =======//

    void addButtons() {
        captureButton = new ImageView(this);
        closeButton = new ImageView(this);
        closeButton.setImageResource(android.R.drawable.ic_delete);

        buttonParams = new WindowManager.LayoutParams(dpToPx(40), dpToPx(40),
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        buttonParams.verticalMargin = dpToPx(8);

        rootLayout.addView(captureButton, buttonParams);
        rootLayout.addView(closeButton, buttonParams);

        RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) captureButton.getLayoutParams();
        params1.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params1.addRule(RelativeLayout.CENTER_HORIZONTAL);
        captureButton.setLayoutParams(params1);


        RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) closeButton.getLayoutParams();
        params3.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params3.addRule(RelativeLayout.ALIGN_PARENT_END);
        closeButton.setLayoutParams(params3);

        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            camera.takePicture(null, null, mPicture);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mCamera.stopPreview();
                                    mCamera.startPreview();

                                }
                            }, 1000);
                    }
                }
        );

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopSelf();
            }
        });


    }

    void addDragFunction() {
        rootLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    xMargin = (int) motionEvent.getX();
                    yMargin = (int) motionEvent.getY();
                    dragFlag = rootParams.height - yMargin < dpToPx(20) && rootParams.width - xMargin < dpToPx(20);
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    int rawX = (int) motionEvent.getRawX();
                    int rawY = (int) motionEvent.getRawY();
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();
                    WindowManager.LayoutParams rootParams = (WindowManager.LayoutParams) rootLayout.getLayoutParams();

                    if (dragFlag && !isRecording) {
                        rootParams.width = x;
                        rootParams.height = y;
                    } else {
                        rootParams.x = rawX - xMargin;
                        rootParams.y = rawY - yMargin - statusBarHeight;
                    }
                    windowManager.updateViewLayout(rootLayout, rootParams);

                }

                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseCamera();
        if (rootLayout != null) {
            windowManager.removeView(rootLayout);
        }

    }



    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }



    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "cameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + format_type);
        return mediaFile;
    }




    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
