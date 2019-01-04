package com.example.wangchao.androidcamera1view.camera.controller;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.cameraview.CameraView;
import com.google.android.cameraview.AspectRatio;

public interface CameraContract {

    interface Presenter {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
        void onResume();
        void onPause();
        void takePictureOrVideo();
        void switchCameraId(int cameraId);
        void setFlashAuto(int autoFlash);
        void setPictureCallBack();
        void switchCameraMode(int currentMode);
        void stopRecord();
        void restartRecord();
        int  getCameraMode();
        void setCurrentAspectRatio(AspectRatio aspectRatio);
        void setViewShowOrHide(View view,boolean isShow);
        void setRecentlyPhotoPath(String recentlyPhotoPath);
        void onReleaseRecord();
        void setZoom(float zoomValues);
        float getZoom();
        float getMaxZoom();
        void setFocusMode(boolean focusMode);
        boolean getFocusMode();
        void focusOnTouch(MotionEvent event, int viewWidth, int viewHeight);
    }

    interface CameraViewCall<T extends Presenter> {
        CameraView getCameraView();
        ImageView  getCameraThumbView();
        /**
         * 加载拍照的图片路径
         * @param filePath
         */
        void loadPictureResult(String filePath);
        /**
         * 显示录像计时时间
         * @param timing
         */
        void setTimeShow(String timing);
        /**
         *切换到录制状态
         * @param  mode
         *
         */
        void switchRecordMode(int mode);
        /**
         * 视频录制的三种状态,开始，停止，完成
         */
        int MODE_RECORD_START=1;
        int MODE_RECORD_STOP=2;
        int MODE_RECORD_FINISH=3;
    }

}
