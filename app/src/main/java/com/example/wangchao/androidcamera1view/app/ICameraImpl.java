package com.example.wangchao.androidcamera1view.app;

import android.app.Activity;

import com.example.cameraview.CameraView;
import com.example.wangchao.androidcamera1view.camera.CameraManager;
import com.example.wangchao.androidcamera1view.camera.event.GlobalHandler;
import com.example.wangchao.androidcamera1view.presenter.CameraPresenter;
import com.example.wangchao.androidcamera1view.utils.sp.PreferencesUtils;
import com.example.wangchao.androidcamera1view.utils.thread.WorkThreadManager;

public interface ICameraImpl {
    Activity getActivity();
    PreferencesUtils getPreferencesUtils();
    CameraManager getCameraManager();
    CameraPresenter getCameraPresenter();
    int getCameraFacingId();
    WorkThreadManager getWorkThreadManager();
    CameraView getCameraView();
    GlobalHandler getGlobalHandler();
}
