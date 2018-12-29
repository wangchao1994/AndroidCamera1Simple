package com.example.wangchao.androidcamera1view.camera.controller;

import com.example.cameraview.CameraView;

public interface CameraContract {

    interface Presenter {
        void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults);
        void onResume();
        void onPause();
        void takePicture();
        void switchCameraId(int direction);
        void setFlashAuto(int autoFlash);
        void setPictureCallBack();
        void switchCameraMode(int currentMode);
        void startVideoRecord();
        void stopRecord();
        void restartRecord();

    }

    interface CameraViewCall<T extends Presenter> {
        CameraView getCameraView();
        /**
         * 加载拍照的图片路径
         * @param filePath
         */
        void loadPictureResult(String filePath);
        /**
         * 显示计时时间
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
