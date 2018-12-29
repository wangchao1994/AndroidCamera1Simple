package com.example.wangchao.androidcamera1view.camera;

import android.util.Log;

import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.camera.mode.PhotoMode;
import com.example.wangchao.androidcamera1view.camera.mode.VideoMode;

public class CameraManager {
    public static final String TAG = CameraManager.class.getSimpleName();
    private ICameraImpl mICameraImpl;
    private CameraModeBase mCurrentMode;
    private CameraModeBase mPhotoMode;
    private CameraModeBase mVideoMode;
    public static final int  MODE_CAMERA = 1;//拍照模式
    public static final int MODE_VIDEO_RECORD = 2;//录像模式
    private int currentCameraDirection;
    public CameraManager(ICameraImpl iCameraImpl){
        mICameraImpl = iCameraImpl;
        mPhotoMode = new PhotoMode(mICameraImpl);
        mVideoMode = new VideoMode(mICameraImpl);
        //默认为拍照模式
        mCurrentMode = mPhotoMode;
    }

    /**
     * 设置拍照回掉
     */
    public void setCameraResultCallBack(CameraModeBase.CameraPictureResultCallBack cameraPictureResultCallBack) {
        mPhotoMode.setCameraPictureResultCallBack(cameraPictureResultCallBack);
    }

    /**
     * 设置CameraView
     * @param cameraView
     */
    public void setCameraView(CameraView cameraView){
        mPhotoMode.setWeakReference(cameraView);
        mVideoMode.setWeakReference(cameraView);
        openCamera();
    }
    /**
     * 停止Camera相关操作
     */
    public void onPauseOperate(){
        mCurrentMode.stopOperate();
    }
    /**
     * openCamera
     */
    public void openCamera(){
        mCurrentMode.startOperate();
    }
    /**
     * 获取当前CameraId
     * @return
     */
    public int getCameraFacingId(){
        return mCurrentMode.getCameraFacingId();
    }
    /**
     * 切换前后摄
     * @param direction
     */
    public void switchCameraDirection(int direction){
        if (currentCameraDirection == direction){
            return;
        }
        mCurrentMode.switchCameraId(direction);
    }
    /**
     *拍照
     */
    public void shutterPicture(){
        mCurrentMode.cameraPhotoClick();
    }
    /**
     *拍照
     */
    public void addPictureCallBack(){
        mCurrentMode.addCameraPictureCallBack();
    }

    /**
     *设置闪光灯
     */
    public void setAutoFlash(int autoFlash){
        mCurrentMode.setAutoFlash(autoFlash);
    }

    /**
     *设置CamereMode
     */
    public void switchCamerMode(int currentMode){
        switch (currentMode) {
            //切换到拍照模式
            case MODE_CAMERA:
                mVideoMode.stopOperate();
                mCurrentMode = mPhotoMode;
                Log.d(TAG,"currentMode-----mPhotoMode--="+currentMode);
                break;
            //切换到录像模式
            case MODE_VIDEO_RECORD:
                mPhotoMode.stopOperate();
                mCurrentMode = mVideoMode;
                Log.d(TAG,"currentMode-----mVideoMode--="+currentMode);
                break;
            default:
                break;
        }
        mCurrentMode.startOperate();
    }

    /**
     * 开始录像
     */
    public void startRecord(){
        mCurrentMode.cameraVideoClick();
    }
    /**
     * 暂停录像
     */
    public  void pauseVideoRecord(){
        ((VideoMode) mVideoMode).pauseRecordingVideo();
    }

}
