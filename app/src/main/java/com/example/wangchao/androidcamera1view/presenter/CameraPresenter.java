package com.example.wangchao.androidcamera1view.presenter;

import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.cameraview.utils.CameraUtils;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.controller.CameraContract;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.utils.glide.GlideLoader;
import com.example.wangchao.androidcamera1view.utils.permission.PermissionsManager;
import com.example.wangchao.androidcamera1view.utils.time.TimingUtils;
import com.example.wangchao.androidcamera1view.utils.toast.ToastUtils;
import com.google.android.cameraview.AspectRatio;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class CameraPresenter implements CameraContract.Presenter,CameraModeBase.CameraPictureOrVideoResultCallBack,CameraModeBase.CameraVideoRecordCallBack{
    private static final String TAG = CameraManager.class.getSimpleName();
    private CameraContract.CameraViewCall mCameraView;
    private ICameraImpl mICameraImp;
    private CameraManager mCameraManager;
    private CompositeSubscription compositeSubscription;
    private int currentCameraMode;//默认拍照模式
    private Subscription cycleTimeSubscription;
    private long time = 0;

    public CameraPresenter(CameraContract.CameraViewCall cameraView, ICameraImpl iCameraImp){
        mICameraImp = iCameraImp;
        mCameraView = cameraView;
        compositeSubscription = new CompositeSubscription();
        mCameraManager = mICameraImp.getCameraManager();
        mCameraManager.setCameraResultCallBack(this);
        mCameraManager.setCameraVideoCallBack(this);
        currentCameraMode = CameraManager.MODE_CAMERA;
    }
    @Override
    public void onResume() {
        if (mCameraManager != null){
            mCameraManager.setCameraView(mCameraView.getCameraView());
        }
    }

    @Override
    public void onPause() {
        if (mCameraManager != null){
            mCameraManager.onPauseOperate();
        }
    }

    @Override
    public void takePictureOrVideo() {
        if (mCameraManager != null){
            mCameraManager.shutterPictureOrVideo();
        }
    }

    @Override
    public void switchCameraId(int cameraId) {
        if (mCameraManager != null){
            mCameraManager.switchCameraDirection(cameraId);
        }
    }

    @Override
    public void setFlashAuto(int autoFlash) {
        if (mCameraManager != null){
            mCameraManager.setAutoFlash(autoFlash);
        }
    }

    @Override
    public void setPictureCallBack() {
        if (mCameraManager != null){
            mCameraManager.addPictureCallBack();
        }
    }

    @Override
    public void switchCameraMode(int currentMode) {
        if (currentMode == currentCameraMode){
            return;
        }
        currentCameraMode = currentMode;
        if (mCameraManager != null){
            mCameraManager.switchCamerMode(currentCameraMode);
        }
    }

    @Override
    public void startRecord() {
        mCameraView.switchRecordMode(CameraContract.CameraViewCall.MODE_RECORD_START);
        cycleTimeSubscription = Observable.interval(1, TimeUnit.SECONDS, Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.computation())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        time += 1000;
                        String time_show = TimingUtils.getDate(time);
                        Log.d(TAG,"startRecord---------="+time_show);
                        mCameraView.setTimeShow(time_show);
                    }
                });
        compositeSubscription.add(cycleTimeSubscription);

    }

    @Override
    public void finishRecord() {
        mCameraView.switchRecordMode(CameraContract.CameraViewCall.MODE_RECORD_FINISH);
        if (cycleTimeSubscription != null) {
            compositeSubscription.remove(cycleTimeSubscription);
        }
        time = 0;
    }

    @Override
    public void stopRecord() {
        if (cycleTimeSubscription != null) {
            compositeSubscription.remove(cycleTimeSubscription);
        }
        mCameraView.switchRecordMode(CameraContract.CameraViewCall.MODE_RECORD_STOP);
        if (mCameraManager != null){
            mCameraManager.pauseVideoRecord();
        }
    }

    @Override
    public void restartRecord() {
        if (mCameraManager != null){
            mCameraManager.shutterPictureOrVideo();
        }
    }

    @Override
    public int getCameraMode() {
        return currentCameraMode;
    }

    @Override
    public void setCurrentAspectRatio(AspectRatio aspectRatio) {
        if (mCameraManager != null){
            mCameraManager.setAspectRatio(aspectRatio);
        }
    }

    @Override
    public void setViewShowOrHide(View view,boolean isShow) {
        if (view != null){
            if (isShow){
                view.setVisibility(View.VISIBLE);
            }else{
                view.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setRecentlyPhotoPath(String recentlyPhotoPath) {
        Log.d(TAG,"recentlyPhotoPath----->"+recentlyPhotoPath);
        ImageView cameraThumbView = mCameraView.getCameraThumbView();
        if (cameraThumbView != null){
            GlideLoader.loadNetWorkResource(BaseApplication.getInstance(),recentlyPhotoPath,cameraThumbView);
        }
    }

    /**
     * 释放录像
     */
    @Override
    public void onReleaseRecord() {
        if (mCameraManager != null){
            mCameraManager.onReleaseRecord();
        }
    }

    @Override
    public void setZoom(float zoomValues) {
        if (mCameraManager != null){
            mCameraManager.setZoomValues(zoomValues);
        }
    }

    @Override
    public float getZoom() {
        if (mCameraManager != null){
            return mCameraManager.getZoomValues();
        }
        return 1.0f;
    }

    @Override
    public void setFocusMode(boolean focusMode) {
        if (mCameraManager != null){
            mCameraManager.setFocusMode(focusMode);
        }
    }

    @Override
    public boolean getFocusMode() {
        if (mCameraManager != null){
            return mCameraManager.getFocusMode();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PermissionsManager.CAMERA_REQUEST_CODE:
                //权限请求失败
                if (grantResults.length == PermissionsManager.CAMERA_REQUEST.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            ToastUtils.showToast(BaseApplication.getInstance(), "拍照权限被拒绝");
                            break;
                        }
                    }
                }
                break;
            case PermissionsManager.VIDEO_REQUEST_CODE:
                if (grantResults.length == PermissionsManager.VIDEO_PERMISSIONS.length) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            ToastUtils.showToast(BaseApplication.getInstance(), "录像权限被拒绝");
                            break;
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void callResultBack(Observable<String> result) {
        if (result != null) {
            Log.d("callResultBack","callResultBack result="+result);
            Subscription subscription = result.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String filePath) {
                            //通知图库，用于刷新
                            CameraUtils.sendBroadcastNotify(BaseApplication.getInstance(), filePath);
                            mCameraView.loadPictureResult(filePath);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            //写入图片到磁盘失败
                            ToastUtils.showToast(BaseApplication.getInstance(), "写入磁盘失败");
                        }
                    });
            compositeSubscription.add(subscription);
        }
    }
}
