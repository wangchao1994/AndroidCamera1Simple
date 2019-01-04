package com.example.wangchao.androidcamera1view.camera.mode;

import android.util.Log;
import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.camera.controller.widget.RotateProgress;
import com.example.wangchao.androidcamera1view.camera.event.GlobalAction;
import com.example.wangchao.androidcamera1view.utils.rxjava.ObservableBuilder;
import com.google.android.cameraview.AspectRatio;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class VideoMode extends CameraModeBase {
    private static final String TAG = VideoMode.class.getSimpleName();
    private ICameraImpl mICameraImpl;
    private List<String> oldVideoPath;
    private CompositeSubscription compositeSubscription;
    private String mNextVideoAbsolutePath;
    private RotateProgress mRotateProgress;

    public VideoMode(ICameraImpl iCameraIml) {
        mICameraImpl = iCameraIml;
        oldVideoPath = new CopyOnWriteArrayList<>();
        compositeSubscription = new CompositeSubscription();
        mRotateProgress = new RotateProgress(iCameraIml.getActivity());
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
        if (cameraView != null) {
            cameraView.start();
        }
    }

    @Override
    public void stopOperate() {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.stop();
        }
    }

    @Override
    public void startOperate() {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.start();
        }
    }

    @Override
    public void setAutoFlash(int autoFlash) {
        CameraView cameraView = getCameraView();
        if (cameraView != null){
            cameraView.setFlash(autoFlash);
        }
    }

    @Override
    public Set<AspectRatio> getSupportedAspectRatios() {
        return null;
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
    public void addCameraPictureCallBack() {
        CameraView cameraView = getCameraView();
        if (cameraView != null) {
            cameraView.addCallback(mPictureCallback);
        }
    }

    @Override
    protected void writePictureData(byte[] data) {
    }
    @Override
    public void cameraPhotoOrVideoClick() {
        Log.d(TAG,"getCameraView().isRecording()="+getCameraView().isRecording());
        if (getCameraView() != null && getCameraView().isRecording()) {
            stopRecordingVideo(true);
        } else {
            startRecordingVideo();
        }
    }
    /**
     * 停止录像
     * 状态:暂停/结束
     * @param isFinishing
     */
    private void stopRecordingVideo(final boolean isFinishing) {
        final CameraView cameraView = getCameraView();
        if (cameraView == null) return;
        cameraView.stopRecording();
        if (isFinishing){
            if (mICameraImpl.getGlobalHandler() != null){
                mICameraImpl.getGlobalHandler().sendEmptyMessage(GlobalAction.SAVE_VIDEO_DIALOG_SHOW);
            }
        }
        /**视频合并 start-------------------------------------------------------*/
        mNextVideoAbsolutePath = cameraView.getNextVideoPath();
        Log.d("stopRecordingVideo","nextVideoPath===="+mNextVideoAbsolutePath);
        Subscription subscription = Observable
                //延迟三十毫秒
                .timer(30, TimeUnit.MICROSECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (isFinishing) {//结束操作
                            Log.i(TAG, "stopRecordingVideo recording complete--------");
                            if (mCameraVideoResultCallBack != null){
                                mCameraVideoResultCallBack.finishRecord();
                            }
                            mergeMultipleFileCallBack();
                            mNextVideoAbsolutePath = null;
                            oldVideoPath.clear();
                        } else {//暂停操作
                            Log.i(TAG, "pauseRecordingVideo recording stop--------");
                            //若是开始新的录制，原本暂停产生的多个文件合并成一个文件。
                            oldVideoPath.add(mNextVideoAbsolutePath);
                            if (oldVideoPath.size() > 1) {
                                mergeMultipleFile();
                            }
                            mNextVideoAbsolutePath = null;
                        }
                    }
                });
        compositeSubscription.add(subscription);
        /*****视频合并 end-------------------------------------------------------****/
    }
    /**
     * 完成录制，输出最终的视频录制文件
     */
    private void mergeMultipleFileCallBack() {
        Log.d("stopRecordingVideo","oldVideoPath.size()====="+oldVideoPath.size());
        if (oldVideoPath.size() > 0) {
            Log.i(TAG, " mergeMultipleFileCallBack file.size()===" + oldVideoPath.size());
            Subscription subscription = ObservableBuilder.createMergeMuiltFile(BaseApplication.getInstance(), oldVideoPath.get(0), mNextVideoAbsolutePath)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            Log.d("mCameraPitureOrVideo","mCameraPitureOrVideoResultCallBack="+mCameraPitureOrVideoResultCallBack);
                            if (mCameraPitureOrVideoResultCallBack != null) {
                                mCameraPitureOrVideoResultCallBack.callResultBack(ObservableBuilder.createVideo(s));
                            }
                            Log.i(TAG, " mergeMultipleFileCallBack--------success-------------video_path = s = "+s);
                        }
                    });
            compositeSubscription.add(subscription);
        } else {
            if (mCameraPitureOrVideoResultCallBack != null) {
                mCameraPitureOrVideoResultCallBack.callResultBack(ObservableBuilder.createVideo(mNextVideoAbsolutePath));
            }
            Log.d(TAG,"video file save path======>"+mNextVideoAbsolutePath);
        }

    }
    /**
     * 暂停后又从新恢复录制，合并多个视频文件
     */
    private void mergeMultipleFile() {
        Log.i(TAG, " mergeMultipleFile  start--->：file.size()= " + oldVideoPath.size());
        Subscription subscription = ObservableBuilder.createMergeMuiltFile(BaseApplication.getInstance(), oldVideoPath.get(0), oldVideoPath.get(1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String filePath) {
                        oldVideoPath.clear();
                        oldVideoPath.add(filePath);
                        Log.i(TAG, " mergeMultipleFile  complete： file.size()" + oldVideoPath.size());
                    }
                });

        compositeSubscription.add(subscription);
    }
    /**
     * 暂停录制
     */
    public void pauseRecordingVideo() {
        stopRecordingVideo(false);
    }
    /**
     * 开始视频录制
     */
    private void startRecordingVideo() {
        if (getCameraView() != null ){
            getCameraView().startRecording();
            if (mCameraVideoResultCallBack != null) {
                mCameraVideoResultCallBack.startRecord();
            }
        }
    }
    /**
     * 返回RotateProgress
     * @return
     */
    public RotateProgress getRotateProgress(){
        return mRotateProgress;
    }

    /**
     * 释MediaRecorder
     */
    public void onReleaseMediaRecord(){
        CameraView cameraView = getCameraView();
        Log.d("wangchao_camera","cameraView.isRecording()====="+cameraView.isRecording());
        if (cameraView != null && cameraView.isRecording()){
            stopRecordingVideo(true);
        }
    }
}
