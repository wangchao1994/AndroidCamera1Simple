package com.example.wangchao.androidcamera1view.presenter;

import android.content.pm.PackageManager;
import android.util.Log;

import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseApplication;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.controller.CameraContract;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.utils.CameraUtils;
import com.example.wangchao.androidcamera1view.utils.permission.PermissionsManager;
import com.example.wangchao.androidcamera1view.utils.toast.ToastUtils;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class CameraPresenter implements CameraContract.Presenter,CameraModeBase.CameraPictureResultCallBack {

    private CameraContract.CameraViewCall mCameraView;
    private ICameraImpl mICameraImp;
    private CameraManager mCameraManager;
    private CompositeSubscription compositeSubscription;
    private int currentCameraMode;//默认拍照模式


    public CameraPresenter(CameraContract.CameraViewCall cameraView, ICameraImpl iCameraImp){
        mICameraImp = iCameraImp;
        mCameraView = cameraView;
        compositeSubscription = new CompositeSubscription();
        mCameraManager = mICameraImp.getCameraManager();
        mCameraManager.setCameraResultCallBack(this);
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
    public void takePicture() {
        if (mCameraManager != null){
            mCameraManager.shutterPicture();
        }
    }

    @Override
    public void switchCameraId(int direction) {
        if (mCameraManager != null){
            mCameraManager.switchCameraDirection(direction);
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
        if (mCameraManager != null){
            mCameraManager.startRecord();
        }
    }

    @Override
    public void stopRecord() {
        if (mCameraManager != null){
            mCameraManager.pauseVideoRecord();
        }
    }

    @Override
    public void restartRecord() {
        if (mCameraManager != null){
            mCameraManager.startRecord();
        }
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
    public void callPictureBack(Observable<String> result) {
        if (result != null) {
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
