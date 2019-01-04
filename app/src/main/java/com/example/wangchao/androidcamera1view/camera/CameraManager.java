package com.example.wangchao.androidcamera1view.camera;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import com.example.cameraview.CameraView;
import com.example.cameraview.utils.file.FileUtils;
import com.example.wangchao.androidcamera1view.app.ICameraImpl;
import com.example.wangchao.androidcamera1view.camera.controller.CameraModeBase;
import com.example.wangchao.androidcamera1view.camera.mode.PhotoMode;
import com.example.wangchao.androidcamera1view.camera.mode.VideoMode;
import com.google.android.cameraview.AspectRatio;

public class CameraManager {

    public static final String TAG = CameraManager.class.getSimpleName();
    private static CameraManager mCameraManager;
    private ICameraImpl mICameraImpl;
    private CameraModeBase mCurrentMode;
    private CameraModeBase mPhotoMode;
    private CameraModeBase mVideoMode;
    public static final int  MODE_CAMERA = 1;//拍照模式
    public static final int MODE_VIDEO_RECORD = 2;//录像模式
    private String mPhotoPathId;
    private String mPhotoPath;
    private String mPhotoSize;
    private CameraManager(ICameraImpl iCameraImpl){
        mICameraImpl = iCameraImpl;
        mPhotoMode = new PhotoMode(mICameraImpl);
        mVideoMode = new VideoMode(mICameraImpl);
        //默认为拍照模式
        mCurrentMode = mPhotoMode;
    }
    public static CameraManager getCameraManagerInstance(ICameraImpl iCameraImpl) {
        if (null == mCameraManager){
            synchronized (CameraManager.class){
                if (null == mCameraManager){
                    mCameraManager = new CameraManager(iCameraImpl);
                }
            }
        }
        return mCameraManager;
    }
    /**
     * 设置拍照回调
     */
    public void setCameraResultCallBack(CameraModeBase.CameraPictureOrVideoResultCallBack cameraPictureOrVideoResultCallBack) {
        mPhotoMode.CameraPictureOrVideoResultCallBack(cameraPictureOrVideoResultCallBack);
        mVideoMode.CameraPictureOrVideoResultCallBack(cameraPictureOrVideoResultCallBack);
    }
    /**
     * 设置录像回调
     */
    public void setCameraVideoCallBack(CameraModeBase.CameraVideoRecordCallBack cameraVideoCallBack){
        mVideoMode.setCameraVideoResultCallBack(cameraVideoCallBack);
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
     * @param cameraId
     */
    public void switchCameraDirection(int cameraId){
        mCurrentMode.switchCameraId(cameraId);
    }
    /**
     *拍照/录像
     */
    public void shutterPictureOrVideo(){
        mCurrentMode.cameraPhotoOrVideoClick();
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
     * 暂停录像
     */
    public  void pauseVideoRecord(){
        ((VideoMode) mVideoMode).pauseRecordingVideo();
    }

    /**
     * save Video Dialog
     */
    public void showProgress(String msg) {
        ((VideoMode) mVideoMode).getRotateProgress().showProgress(msg);
    }
    public void dismissProgress() {
        ((VideoMode) mVideoMode).getRotateProgress().hide();
    }
    public boolean isShowingProgress() {
        return ((VideoMode) mVideoMode).getRotateProgress().isShowing();
    }

    /**
     * get AspectRatio
     * @return
     */
    public AspectRatio getCurrentSupportAspectRatio(){
        AspectRatio currentAspectRatio = mCurrentMode.getCurrentAspectRatio();
        return currentAspectRatio;
    }
    public void setAspectRatio(AspectRatio ratio){
        mCurrentMode.setCurrentAspectRatio(ratio);
    }
    /**
     * 获取最近一次拍照的图片ID
     * @param context
     * @return
     */
    public String getRecentlyPhotoPath(Context context) {
        //String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + "/DCIM/Camera/" + "%' ";
        String searchPath = MediaStore.Files.FileColumns.DATA + " LIKE '%" + FileUtils.DIRECTORY + "%' ";
        Uri uri = MediaStore.Files.getContentUri("external");
        Cursor cursor = context.getContentResolver().query(
                uri, new String[]{MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DATA,MediaStore.Files.FileColumns.SIZE}, searchPath, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            mPhotoPathId = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
            mPhotoPath =  cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
            mPhotoSize =  cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return mPhotoPath;
    }
    public void onReleaseRecord(){
        ((VideoMode) mVideoMode).onReleaseMediaRecord();
    }

    /**
     * 设置缩放比例
     * @param zoomValues
     */
    public void setZoomValues(float zoomValues){
        mCurrentMode.setZoomValues(zoomValues);
    }
    public float getZoomValues(){
        return mCurrentMode.getZoomValues();
    }
}
