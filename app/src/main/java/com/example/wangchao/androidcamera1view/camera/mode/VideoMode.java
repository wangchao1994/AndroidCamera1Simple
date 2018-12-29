package com.example.wangchao.androidcamera1view.camera.mode;

import android.hardware.Camera;
import android.media.MediaRecorder;
import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.google.android.cameraview.AspectRatio;
import java.util.Set;

public class VideoMode extends CameraModeBase {
    public static final String TAG = VideoMode.class.getSimpleName();
    private ICameraImpl mICameraImpl;
    private CameraManager mCameraManager;
    /**
     * 当前是否是在录制视频
     */
    private boolean mIsRecordingVideo;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;

    public VideoMode(ICameraImpl iCameraIml){
        mICameraImpl = iCameraIml;
        mCameraManager = mICameraImpl.getCameraManager();
    }

    @Override
    public void switchCameraId(int cameraId) {
    }

    @Override
    public int getCameraFacingId() {
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
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.start();
        }
    }

    @Override
    public void setAutoFlash(int autoFlash) {
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatios() {
        return null;
    }

    @Override
    public AspectRatio getCurrentAspectRatio() {
        return null;
    }

    @Override
    public void cameraPhotoClick() {
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
    }

    @Override
    public void cameraVideoClick() {
        if (mIsRecordingVideo) {
            stopRecordingVideo(true);
        } else {
            startRecordingVideo();
        }
    }

    /**
     * 结束录像
     * @param isFinishing
     */
    private void stopRecordingVideo(boolean isFinishing) {

    }

    /**
     * 开始视频录制
     */
    private void startRecordingVideo() {

    }
    /**
     * 暂停录制
     */
    public void pauseRecordingVideo() {
        stopRecordingVideo(false);
    }
}
