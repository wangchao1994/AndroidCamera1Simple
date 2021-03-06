package com.example.wangchao.androidcamera1view.camera.controller;

import android.view.MotionEvent;

import com.example.cameraview.CameraView;
import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CallbackBridge;

import java.lang.ref.WeakReference;
import rx.Observable;

public abstract class CameraModeBase {

    //CameraView------------------------------start-------------------------------------------------------
    /**
     * 设置CameraView相关
     * 供模式内使用
     */
    private WeakReference<CameraView> weakReference;
    protected CameraView getCameraView() {
        return weakReference != null ? weakReference.get() : null;
    }
    public void setWeakReference(CameraView cameraView) {
        if (getCameraView()==null){
            weakReference = new WeakReference<>(cameraView);
        }
    }
    //CameraView---------------------------------end----------------------------------------------------

    //CameraId---------------------------------start----------------------------------------------------
    /**
     * CameraId switch
     */
    public abstract void switchCameraId(int cameraId);
    public abstract int getCameraFacingId();
    //CameraId---------------------------------end------------------------------------------------------

    /**
     * openCamera
     */
    public abstract void openCamera();
    /**
     * 停止操作
     */
    public  abstract void stopOperate();
    /**
     * 开始操作
     */
    public abstract void startOperate();

    /**
     * 设置闪光灯
     */
    public abstract void setAutoFlash(int autoFlash);

    /**
     * 分辨率
     */
    public abstract AspectRatio getCurrentAspectRatio();
    public abstract void setCurrentAspectRatio(AspectRatio aspectRatio);

    /**
     * ZOOM
     * @param zoomValues
     */
    public abstract void setZoomValues(float zoomValues);
    public abstract float getZoomValues();
    public abstract float getMaxZoomValues();
    /**
     * Focus
     * @param isAutoFocusMode
     */
    public abstract void setFocusMode(boolean isAutoFocusMode);
    public abstract boolean getFocusMode();
    public abstract void handleFocus(MotionEvent event);
    /**
     *点击拍照Or录像事件
     */
    public abstract void cameraPhotoOrVideoClick();
    //Camera图片数据-----------------------------------------

    public abstract void addCameraPictureCallBack();
    /**
     * 写入图片数据
     * @param data
     */
    protected abstract void writePictureData(byte[] data);
    protected  CallbackBridge.Callback mPictureCallback = new CallbackBridge.Callback() {
        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
        }

        @Override
        public void onPictureTaken(CameraView cameraView, byte[] data) {
            super.onPictureTaken(cameraView, data);
            writePictureData(data);
        }

        @Override
        public void onPreviewFrame(CameraView cameraView, byte[] data) {
            super.onPreviewFrame(cameraView, data);
            onCallPreviewFrame(data);
        }
    };
    //预览流
    protected abstract void onCallPreviewFrame(byte[] data);
    //Camera图片数据-----------------------------------------

    public CameraPictureOrVideoResultCallBack mCameraPitureOrVideoResultCallBack;
    public void CameraPictureOrVideoResultCallBack(CameraPictureOrVideoResultCallBack cameraPictureOrVideoResultCallBack) {
        mCameraPitureOrVideoResultCallBack = cameraPictureOrVideoResultCallBack;
    }
    //录像拍照公共接口
    public interface CameraPictureOrVideoResultCallBack{
        void callResultBack(Observable<String> result);
    }
    //Camera录像数据-----------------------------------------

    public CameraVideoRecordCallBack mCameraVideoResultCallBack;
    public void setCameraVideoResultCallBack(CameraVideoRecordCallBack cameraVideoResultCallBack) {
        mCameraVideoResultCallBack = cameraVideoResultCallBack;
    }
    /**
     *录像事件
     */
    public interface  CameraVideoRecordCallBack{
        /**
         *  开始录制
         */
        void startRecord();
        /**
         * 完成录制
         */
        void finishRecord();
    }
    //Camera录像数据-----------------------------------------

}
