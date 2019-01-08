package com.example.wangchao.androidcamera1view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.base.BaseActivity;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.event.GlobalAction;
import com.example.wangchao.androidcamera1view.camera.event.GlobalHandler;
import com.example.wangchao.androidcamera1view.presenter.CameraPresenter;
import com.example.wangchao.androidcamera1view.utils.sp.PreferencesUtils;
import com.example.wangchao.androidcamera1view.utils.thread.WorkThreadManager;

public class CameraActivity extends BaseActivity implements ICameraImpl {
    private static final String TAG = CameraActivity.class.getSimpleName();
    private CameraFragment cameraFragment;
    private CameraPresenter mCameraPresenter;
    private CameraManager mCameraManager;
    private WorkThreadManager mWorkThreadManager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_camera;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        cameraFragment = (CameraFragment) getSupportFragmentManager().findFragmentByTag(CameraFragment.TAG);
        if (cameraFragment == null) {
            cameraFragment = CameraFragment.newInstance(this);
            getSupportFragmentManager().beginTransaction().add(R.id.container, cameraFragment, CameraFragment.TAG).addToBackStack(null).commitAllowingStateLoss();
        }
        mCameraPresenter = new CameraPresenter(cameraFragment,this);
    }

    @Override
    protected void initDataManager() {
        mCameraManager = CameraManager.getCameraManagerInstance(this);
        mWorkThreadManager = WorkThreadManager.getWorkThreadManagerInstance();
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public PreferencesUtils getPreferencesUtils() {
        if (mPreferencesUtils != null){
            return mPreferencesUtils;
        }
        return null;
    }

    @Override
    public CameraManager getCameraManager() {
        if (mCameraManager != null){
            return mCameraManager;
        }
        return null;
    }

    @Override
    public CameraPresenter getCameraPresenter() {
        if (mCameraPresenter != null){
            return mCameraPresenter;
        }
        return null;
    }

    @Override
    public int getCameraFacingId() {
        if (mCameraManager != null){
            return mCameraManager.getCameraFacingId();
        }
        return 0;
    }

    @Override
    public WorkThreadManager getWorkThreadManager() {
        if (mWorkThreadManager != null){
            return mWorkThreadManager;
        }
        return null;
    }

    @Override
    public CameraView getCameraView() {
        if (cameraFragment!=null){
            return cameraFragment.getCameraView();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mCameraPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    public GlobalHandler getGlobalHandler() {
        if (mGlobalHandler != null){
            return mGlobalHandler;
        }
        return null;
    }
    @Override
    public void handleMsg(Message msg) {
        switch (msg.what){
            case GlobalAction.SAVE_VIDEO_DIALOG_SHOW:
                mCameraManager.showProgress(getResources().getString(R.string.save_video));
                break;
            case GlobalAction.SAVE_VIDEO_DIALOG_DISMISS:
                if(mCameraManager.isShowingProgress()){
                    mCameraManager.dismissProgress();
                }
                break;
        }
    }
}
