package com.example.wangchao.androidcamera1view.camera.mode;

import android.view.MotionEvent;

import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.utils.rxjava.ObservableBuilder;
import com.google.android.cameraview.AspectRatio;

public class PhotoMode extends CameraModeBase {
    public static final String TAG = PhotoMode.class.getSimpleName();
    private ICameraImpl mICameraImpl;

    public PhotoMode(ICameraImpl iCameraIml){
        mICameraImpl = iCameraIml;
    }
    @Override
    public void switchCameraId(int cameraId) {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.setFacing(cameraId);
        }
    }

    @Override
    public int getCameraFacingId() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            int facing = cameraView.getFacing();
            return facing;
        }
        return 0;
    }

    @Override
    public void openCamera() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.start();
        }
    }

    @Override
    public void stopOperate() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.stop();
        }
    }

    @Override
    public void startOperate() {
        openCamera();
    }

    @Override
    public void setAutoFlash(int autoFlash) {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.setFlash(autoFlash);
        }
    }

    @Override
    public AspectRatio getCurrentAspectRatio() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            AspectRatio aspectRatio = cameraView.getAspectRatio();
            return aspectRatio;
        }
        return null;
    }

    @Override
    public void setCurrentAspectRatio(AspectRatio aspectRatio) {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.setAspectRatio(aspectRatio);
        }
    }

    @Override
    public void setZoomValues(float zoomValues) {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.setZoom(zoomValues);
        }
    }

    @Override
    public float getZoomValues() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            return cameraView.getZoom();
        }
        return 1.0f;
    }

    @Override
    public float getMaxZoomValues() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            return cameraView.getMaxZoom();
        }
        return 1.0f;
    }

    @Override
    public void setFocusMode(boolean focusMode) {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.setAutoFocus(focusMode);
        }
    }

    @Override
    public boolean getFocusMode() {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            return cameraView.getAutoFocus();
        }
        return true;
    }

    @Override
    public void handleFocus(MotionEvent event) {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.handleFocus(event);
        }
    }

    @Override
    public void cameraPhotoOrVideoClick() {
        CameraView cameraView = getCameraView();
        if (cameraView!=null){
            cameraView.takePicture();
        }
    }

    @Override
    public void addCameraPictureCallBack() {
        CameraView cameraView = getCameraView();
        if (cameraView!=null){
            cameraView.addCallback(mPictureCallback);
        }
    }

    @Override
    protected void writePictureData(byte[] data) {
        if (mCameraPitureOrVideoResultCallBack != null){
            //数据写入返回路径
            mCameraPitureOrVideoResultCallBack.callResultBack(ObservableBuilder.createWriteCaptureData(BaseApplication.getInstance(), data));
        }
    }


}
